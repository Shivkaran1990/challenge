package com.dws.challenge.domain;

import java.math.BigDecimal;

import lombok.Data;

@Data	
public class MoneyTransferRequest {
	private String accountFromId;
    private String accountToId;	
    private BigDecimal amount;
}
