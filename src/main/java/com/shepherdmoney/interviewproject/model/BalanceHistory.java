package com.shepherdmoney.interviewproject.model;

import java.time.LocalDate;

import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import lombok.Getter;
import lombok.RequiredArgsConstructor;
import lombok.Setter;
import lombok.ToString;

@Entity
@Getter
@Setter
@ToString
@RequiredArgsConstructor
public class BalanceHistory {

    @Id
    @GeneratedValue(strategy = GenerationType.AUTO)
    private int id;
    
    private LocalDate date;

    private double balance;
    
    @ManyToOne
    @JoinColumn(name = "credit_card_id")
    private CreditCard creditCard;

    public BalanceHistory(LocalDate date, double balance, CreditCard creditCard) {
        this.date = date;
        this.balance = balance;
        this.creditCard = creditCard;
    }
}
