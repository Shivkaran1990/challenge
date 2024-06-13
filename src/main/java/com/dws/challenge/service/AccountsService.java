package com.dws.challenge.service;

import java.math.BigDecimal;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import com.dws.challenge.domain.Account;
import com.dws.challenge.domain.MoneyTransferRequest;
import com.dws.challenge.exception.AccountNotFoundException;
import com.dws.challenge.repository.AccountsRepository;

@Service
public class AccountsService {

	@Autowired
	private AccountsRepository accountsRepository;

	@Autowired
	private NotificationService notificationService;

	public void createAccount(Account account) {
		this.accountsRepository.createAccount(account);
	}

	public Account getAccount(String accountId) {
		return this.accountsRepository.getAccount(accountId);
	}

	public void transfer(MoneyTransferRequest request) {
		Account accountFrom = accountsRepository.getAccount(request.getAccountFromId());
		Account accountTo = accountsRepository.getAccount(request.getAccountToId());
		
		// this code will maintain the order of lock, so deadlock can be avoid
		 Account accountFromLock, accountToLock;
		    if (request.getAccountFromId().compareTo(request.getAccountToId()) < 0) {
		    	accountFromLock = accountFrom;
		    	accountToLock = accountTo;
		    } else {
		    	accountFromLock = accountTo;
		    	accountToLock = accountFrom;
		    }
		//this is transation based operation we have to maintain the consistency so at time only 
		 //one thread should be able to perform write operation 
		synchronized (accountFromLock) {
			synchronized (accountToLock) {
				// to get the lasted copy of data in case 2 thread comming at same time.
				 accountFrom = accountsRepository.getAccount(request.getAccountFromId());
				 accountTo = accountsRepository.getAccount(request.getAccountToId());
				accountDataValidation(request, accountFrom, accountTo);
				
				int result=accountFrom.getBalance().compareTo(request.getAmount());
				if ( result<0) {
					throw new IllegalArgumentException("Insufficient balance");
				}
				accountFrom.withdraw(request.getAmount());
				accountTo.deposit(request.getAmount());
				accountsRepository.updateAccount(request.getAccountFromId(), accountFrom);
				accountsRepository.updateAccount(request.getAccountToId(), accountTo);
				notifyUsers(request, accountFrom, accountTo);
			}
		}
	}

	private void accountDataValidation(MoneyTransferRequest request, Account accountFrom, Account accountTo) {
		if (accountFrom == null || accountTo == null) {
			throw new AccountNotFoundException("Not found From or To Account number");
		}

		if (request.getAmount().compareTo(BigDecimal.ZERO) == 0) {
			throw new IllegalArgumentException("Transfer amount must be positive");
		}

		if (accountFrom.getAccountId().equals(accountTo.getAccountId())) {
			throw new IllegalArgumentException("Cannot transfer to the same account");
		}
	}

	private void notifyUsers(MoneyTransferRequest request, Account accountFrom, Account accountTo) {

		notificationService.notifyAboutTransfer(accountFrom,
				"Transferred " + request.getAmount() + " to account " + accountTo.getAccountId());
		notificationService.notifyAboutTransfer(accountTo,
				"Received " + request.getAmount() + " from account " + accountFrom.getAccountId());
	}

}
