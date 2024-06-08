# challenge Changes
 
MoneyTransferRequest.class
this class created to handle the request related attributes.

AccountNotFoundException.class
Custom exeception class created, if we dont have accounts then AccountNotFoundException will thrown.

AccountsRepository.class
Interface update with new method declearation updateAccount.

AccountsRepositoryInMemory.class
updateAccount method implemention provided in Repo class.

AccountsService.class
i have added transfer method in AccountsService class, this methods take MoneyTransferRequest object as input and perform all the validation and perfrom the debit and credit operation
and update in InMemory DB.

AccountsController.class
add put method in controller class to handle the transfer money request.

### Thread Safety
Transfers are handled within synchronized blocks on the involved accounts to ensure thread safety and avoid deadlocks.

### Tests

The tests for the `AccountsServiceTest` mock the `NotificationService` to focus on the transfer logic.

## improvements
Given more time, the following improvements could be made:
- Comprehensive validation: Additional checks for account status (active, suspended)
- Transaction Logging : Implement logging for audit purposes.
- Exception Handling: Improve error handling and create custom exceptions.
- Security: Implement authentication and authorization for transfer endpoints.







