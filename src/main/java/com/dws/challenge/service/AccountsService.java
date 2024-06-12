package com.dws.challenge.service;

import java.math.BigDecimal;
import java.util.concurrent.locks.ReentrantLock;

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

	private final ReentrantLock lock = new ReentrantLock();

	public void createAccount(Account account) {
		this.accountsRepository.createAccount(account);
	}

	public Account getAccount(String accountId) {
		return this.accountsRepository.getAccount(accountId);
	}

	public void transfer(MoneyTransferRequest request) {
		Account accountFrom = accountsRepository.getAccount(request.getAccountFromId());
		Account accountTo = accountsRepository.getAccount(request.getAccountToId());

		accountDataValidation(request, accountFrom, accountTo);

		lock.lock();
		try {
			int result = accountFrom.getBalance().compareTo(request.getAmount());
			if (result < 0) {
				throw new IllegalArgumentException("Insufficient balance");
			}

			accountFrom.withdraw(request.getAmount());
			accountTo.deposit(request.getAmount());

			accountsRepository.updateAccount(request.getAccountFromId(), accountFrom);
			accountsRepository.updateAccount(request.getAccountToId(), accountTo);

			notifyUsers(request, accountFrom, accountTo);
		} finally {
			lock.unlock();
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
