# Shepherd Money Interview Project

## UPDATES
Here is a list of updates we have made to the projects from questions asked by applicants
- 04/28/2024
  - User model does not have to have DOB field. That description has been removed from the README
  - There is a typo in the balance history update method, claiming that the most recent entry should be the first in the list while the example shows otherwise. We have modified our grading script such that both sorting direction works. Just make sure its sorted with one direction.
  - If there is a gap in the balace history list, and we retrieving the balance history of a date that is not recorded, return the value of either the "closest" or "closest previous" would work 

## Introduction

Thanks for your interest in applying to Shepherd Money! Complete this short toy project before your interview to help us evaluate your skills as a software engineer. It shouldn't take more than an hour if you know Spring Boot. We look forward to seeing your work and learning more about you!

## Submission
Create a public repository on Github or Gitlab with the code committed to the `main` branch. Send the repository link to bofanxu@shepherdmoney.com. Make sure that your code is committed to the top level git root.

## Testing and What We Are Looking For
Feel free to test your solution with your own inputs as we don't provide local test cases. To run the project, use the code below. JDK 17 is required, so make sure it's installed on your computer. If you're using Debian Linux, you can install it with `sudo apt install openjdk-17-jdk`.

```bash
./gradlew bootRun
```

We will test your solution by calling the controller APIs directly. While correctness is important, we are more interested in how you designed the API and your coding practices, such as using clear variable names, good comments, and good naming conventions.

## Project Summary
Write a Spring Boot program that manages user creation/deletion and adding credit cards to their profiles. Users may have zero or more credit cards associated with them. Also, create two APIs: one to get all credit cards for a user and another to find a user by their credit card number. There is an additional API that update the balance history of a credit card.

## Files to Change
Any files with content marked with **TODO** will contains hints on what to add. You are welcomed to add/modify any files to help to implement this project.

## Models
Following is a component overview of the models. You can find more hints in the source code files as well
- `User`: each user has their name, and some (or none) credit cards associated with them
- `CreditCard`: each credit card has issance bank, card number, who the card belongs to, and a list of balance history
- `BalanceHistory`: credit card balance for a specific date.

## Controllers
The controllers should contain enough comment for you to implement their basic functionalities. Note that apart from implementing the simple functionalities, we are looking for good coding conventions, good error handling, etc.

## Useful Tools
- **PostMan**: useful to send http requests to test your API
- **H2 Console**: when running your project, you can use `http://localhost:8080/h2-ui` to access the h2 console. This will allow to look at what's stored in the database.
  - The database name is `database`
  - The username is `sa`
  - The password is `password`

## Use of Generative AI Tools
We **welcome** you to use generative AI tools to complete this project :). We believe generative AI tools can be powerful assistants to SDEs, if used appropriately. However, you must **disclose** its usage when you submit your code. Just append a short sentence in your email submission saying that

```
I have used generative AI tools in the process of completing this project.
```

Your chances of getting an interview and/or being given an offer is not affected by this. **However**, as frequent users of generative AI tools, we are keen in spotting traces of generated code. If we believe you have used AI tools without disclosing, we will not move forward with your application.

## Questions?
If you have project related issues, feel free to raise an issue on the repository :D. If you have more private questions, please reach out to bofanxu@shepherdmoney.com