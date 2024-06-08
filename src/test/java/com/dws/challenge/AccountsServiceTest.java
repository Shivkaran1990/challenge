package com.dws.challenge;

import static org.assertj.core.api.Assertions.fail;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.math.BigDecimal;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;

import com.dws.challenge.domain.Account;
import com.dws.challenge.domain.MoneyTransferRequest;
import com.dws.challenge.exception.AccountNotFoundException;
import com.dws.challenge.repository.AccountsRepository;
import com.dws.challenge.service.AccountsService;
import com.dws.challenge.service.NotificationService;

public class AccountsServiceTest {

	@InjectMocks
	private AccountsService accountsService;

	@Mock
	private AccountsRepository accountsRepository;

	@Mock
	private NotificationService notificationService;

	@BeforeEach
	void setUp() {
		MockitoAnnotations.openMocks(this);
	}

	@Test
	void transfer_shouldTransferAmountBetweenAccounts() {
		Account accountFrom = new Account("1");
		accountFrom.setBalance(BigDecimal.valueOf(1000));

		Account accountTo = new Account("2");
		accountTo.setBalance(BigDecimal.valueOf(500));

		MoneyTransferRequest request = new MoneyTransferRequest();
		request.setAccountFromId("1");
		request.setAccountToId("2");
		request.setAmount(BigDecimal.valueOf(200));

		when(accountsRepository.getAccount("1")).thenReturn(accountFrom);
		when(accountsRepository.getAccount("2")).thenReturn(accountTo);

		accountsService.transfer(request);

		verify(accountsRepository).updateAccount("1", accountFrom);
		verify(accountsRepository).updateAccount("2", accountTo);

		verify(notificationService).notifyAboutTransfer(accountFrom, "Transferred 200 to account 2");
		verify(notificationService).notifyAboutTransfer(accountTo, "Received 200 from account 1");
	}

	@Test
	void test_accountIDSameTest() {
		Account account = new Account("1");
		account.setBalance(BigDecimal.valueOf(1000));
		MoneyTransferRequest request = new MoneyTransferRequest();
		request.setAccountFromId("1");
		request.setAccountToId("1");
		request.setAmount(BigDecimal.valueOf(200));
		when(accountsRepository.getAccount("1")).thenReturn(account);
		try {
			accountsService.transfer(request);
			fail("Cannot transfer to the same account");
		} catch (IllegalArgumentException e) {

		}
	}

	@Test
	void test_oneOrBothAccountNotPresent() {
		MoneyTransferRequest request = new MoneyTransferRequest();
		request.setAccountFromId("1");
		request.setAccountToId("2");
		request.setAmount(BigDecimal.valueOf(200));

		when(accountsRepository.getAccount("1")).thenReturn(null);
		when(accountsRepository.getAccount("2")).thenReturn(null);

		try {
			accountsService.transfer(request);
			fail("Cannot transfer to the same account");
		} catch (AccountNotFoundException e) {

		}
	}

	@Test
	void test_transferAmountMustBePositive() {
		Account account = new Account("1");
		account.setBalance(BigDecimal.valueOf(-1));
		MoneyTransferRequest request = new MoneyTransferRequest();
		request.setAccountFromId("1");
		request.setAccountToId("1");
		request.setAmount(BigDecimal.valueOf(200));
		when(accountsRepository.getAccount("1")).thenReturn(account);
		try {
			accountsService.transfer(request);
			fail("Transfer amount must be positive");
		} catch (IllegalArgumentException e) {

		}
	}
}
