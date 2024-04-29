package com.shepherdmoney.interviewproject.controller;

import com.shepherdmoney.interviewproject.repository.CreditCardRepository;
import com.shepherdmoney.interviewproject.repository.UserRepository;
import com.shepherdmoney.interviewproject.model.BalanceHistory;
import com.shepherdmoney.interviewproject.model.CreditCard;
import com.shepherdmoney.interviewproject.model.User;
import com.shepherdmoney.interviewproject.vo.request.AddCreditCardToUserPayload;
import com.shepherdmoney.interviewproject.vo.request.UpdateBalancePayload;
import com.shepherdmoney.interviewproject.vo.response.CreditCardView;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.cglib.core.Local;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDate;
import java.util.Arrays;
import java.util.Comparator;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

@RestController
public class CreditCardController {

    private static final Logger log = LoggerFactory.getLogger(CreditCardController.class);
    // wire in CreditCard repository here (~1 line)
    @Autowired
    CreditCardRepository creditCardRepository;

    @Autowired
    UserRepository userRepository;

    @PostMapping("/credit-card")
    public ResponseEntity<Integer> addCreditCardToUser(@RequestBody AddCreditCardToUserPayload payload) {
        //Create a credit card entity, and then associate that credit card with user with given userId
        //       Return 200 OK with the credit card id if the user exists and credit card is successfully associated with the user
        //       Return other appropriate response code for other exception cases
        //       Do not worry about validating the card number, assume card number could be any arbitrary format and length
        Optional<User> userOptional = userRepository.findById(payload.getUserId());
        if (!userOptional.isPresent()) {
            return ResponseEntity.notFound().build();
        }
        User user = userOptional.get();
        CreditCard creditCard = new CreditCard();
        creditCard.setIssuanceBank(payload.getCardIssuanceBank());
        creditCard.setNumber(payload.getCardNumber());
        creditCard.setUser(user);
        creditCardRepository.save(creditCard);
        return ResponseEntity.ok(creditCard.getId());
    }

    @GetMapping("/credit-card:all")
    public ResponseEntity<List<CreditCardView>> getAllCardOfUser(@RequestParam int userId) {
        //return a list of all credit card associated with the given userId, using CreditCardView class
        //       if the user has no credit card, return empty list, never return null
        List<CreditCard> creditCards = creditCardRepository.findAllByUserId(userId);
        List<CreditCardView> creditCardViews = creditCards.stream()
            .map(card -> CreditCardView.builder()
                .issuanceBank(card.getIssuanceBank())
                .number(card.getNumber())
                .build())
            .collect(Collectors.toList());
        return ResponseEntity.ok(creditCardViews);
    }

    @GetMapping("/credit-card:user-id")
    public ResponseEntity<Integer> getUserIdForCreditCard(@RequestParam String creditCardNumber) {
        // Given a credit card number, efficiently find whether there is a user associated with the credit card
        //       If so, return the user id in a 200 OK response. If no such user exists, return 400 Bad Request
        Optional<CreditCard> creditCardOptional = creditCardRepository.findByNumber(creditCardNumber);
        if (!creditCardOptional.isPresent()) {
            return ResponseEntity.notFound().build();
        }
        User user = creditCardOptional.get().getUser();
        return ResponseEntity.ok(user.getId());
    }

    @Transactional
    @PostMapping("/credit-card:update-balance")
    public ResponseEntity<?> updateCreditCardBalance(@RequestBody UpdateBalancePayload[] payload) {
        Logger log = LoggerFactory.getLogger(getClass());
        //Given a list of transactions, update credit cards' balance history.
        //      1. For the balance history in the credit card
        //      2. If there are gaps between two balance dates, fill the empty date with the balance of the previous date
        //      3. Given the payload `payload`, calculate the balance different between the payload and the actual balance stored in the database
        //      4. If the different is not 0, update all the following budget with the difference
        //      For example: if today is 4/12, a credit card's balanceHistory is [{date: 4/12, balance: 110}, {date: 4/10, balance: 100}],
        //      Given a balance amount of {date: 4/11, amount: 110}, the new balanceHistory is
        //      [{date: 4/12, balance: 120}, {date: 4/11, balance: 110}, {date: 4/10, balance: 100}]
        //      Return 200 OK if update is done and successful, 400 Bad Request if the given card number
        //        is not associated with a card.
        Arrays.sort(payload, Comparator.comparing(UpdateBalancePayload::getBalanceDate));
        for (UpdateBalancePayload transcation : payload) {
            Optional<CreditCard> creditCardOptional = creditCardRepository.findByNumber(transcation.getCreditCardNumber());
            if (!creditCardOptional.isPresent()) {
                return ResponseEntity.notFound().build();
            }
            CreditCard creditCard = creditCardOptional.get();
            List<BalanceHistory> balanceHistory = creditCard.getBalanceHistory();
            //if the balance history is empty, add the first entry
            if (balanceHistory.isEmpty()) {
                log.info("Balance history is initially empty, adding first balance entry");
                BalanceHistory newEntry = new BalanceHistory(transcation.getBalanceDate(), transcation.getBalanceAmount(), creditCard);
                balanceHistory.add(newEntry);
                creditCardRepository.save(creditCard);
                continue;
            }
            // If there are gaps between two balance dates, fill the empty date with the balance of the previous date
            // make sure the previous date is not null
            LocalDate previousDate = balanceHistory.get(0).getDate();
            int index = 0;
            while (previousDate == null) {
                index++;
                if (index < balanceHistory.size()) {
                    previousDate = balanceHistory.get(index).getDate();
                } else{
                    return ResponseEntity.internalServerError().build();
                }
            }
            for (BalanceHistory entry : balanceHistory) {
                if (entry.getDate().isAfter(previousDate.plusDays(1))) {
                    for (LocalDate currDate = previousDate.plusDays(1); currDate.isBefore(entry.getDate()); currDate = currDate.plusDays(1)) {
                        BalanceHistory newEntry = new BalanceHistory(currDate, entry.getBalance(), creditCard);
                        balanceHistory.add(newEntry);
                    }
                } 
                previousDate = entry.getDate();
            }

            //calculate the balance different between the payload and the actual balance stored in the database\
            LocalDate payloadDate = transcation.getBalanceDate();
            Double payloadAmount = transcation.getBalanceAmount();
            Double currentBalance = creditCard.getBalanceHistory().stream()
                .filter(entry -> entry.getDate().equals(payloadDate))
                .map(BalanceHistory::getBalance)
                .findFirst()
                .orElse(null);
            if (currentBalance == null) {
                // since we have filled the gaps
                //now if the current balance is null
                //it means the date is before the first date in the balance history
                //or the date is after the last date in the balance history
                balanceHistory.sort(Comparator.comparing(BalanceHistory::getDate));
                LocalDate firstDate = balanceHistory.get(0).getDate();
                LocalDate lastDate = balanceHistory.get(balanceHistory.size() - 1).getDate();
                if (payloadDate.isBefore(firstDate)) {
                    for (LocalDate currDate = payloadDate; currDate.isBefore(firstDate); currDate = currDate.plusDays(1)) {
                        BalanceHistory newEntry = new BalanceHistory(currDate, payloadAmount, creditCard);
                        balanceHistory.add(newEntry);
                        balanceHistory.sort(Comparator.comparing(BalanceHistory::getDate));
                    }
                } else if (payloadDate.isAfter(lastDate)) {
                    for (LocalDate currDate = lastDate.plusDays(1); currDate.isBefore(payloadDate); currDate = currDate.plusDays(1)) {
                        BalanceHistory newEntry = new BalanceHistory(currDate, balanceHistory.get(balanceHistory.size() - 1).getBalance(), creditCard);
                        balanceHistory.add(newEntry);
                        balanceHistory.sort(Comparator.comparing(BalanceHistory::getDate));
                    }
                    BalanceHistory newEntry = new BalanceHistory(payloadDate, payloadAmount, creditCard);
                    balanceHistory.add(newEntry);
                    balanceHistory.sort(Comparator.comparing(BalanceHistory::getDate));
                }
            } else {
                //If the different is not 0, update all the following budget with the difference
                Double diff = payloadAmount - currentBalance;
                if (diff != 0) {
                    //log.info("Updating balance for date: {}, diff: {}", payloadDate, diff);
                    for (BalanceHistory entry : balanceHistory) {
                        if (entry.getDate().isAfter(payloadDate) || entry.getDate().equals(payloadDate)) {
                            entry.setBalance(entry.getBalance() + diff);
                        }
                    }
                }
            }
            creditCardRepository.save(creditCard);
            //for testing
            for (BalanceHistory history : balanceHistory) {
                log.info("BalanceHistory: Date = {}, Balance = {}", history.getDate(), history.getBalance());
            }
            log.info("Balance updated successfully");
            
        }
        return ResponseEntity.ok("Balance updated successfully");
    }
    
}
