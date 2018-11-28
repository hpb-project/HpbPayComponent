package io.hpb.pay.configure;

import org.springframework.boot.context.properties.ConfigurationProperties;

/**
 * web3 property container.
 */
@ConfigurationProperties(prefix = "web3")
public class Web3Properties {

	public static final String WEB3_PREFIX = "web3";

	private String clientAddress;

	private Boolean adminClient;

	private String networkId;

	private String contractAddr;
	
	private String gasPriceGwei;
	
	private String gasLimitMax;
	
	private String gasLimitMid;
	
	private String gasLimitMin;

	private Long httpTimeoutSeconds;

	public String getClientAddress() {
		return clientAddress;
	}

	public void setClientAddress(String clientAddress) {
		this.clientAddress = clientAddress;
	}

	public Boolean isAdminClient() {
		return adminClient;
	}

	public void setAdminClient(Boolean adminClient) {
		this.adminClient = adminClient;
	}

	public String getNetworkId() {
		return networkId;
	}

	public void setNetworkId(String networkId) {
		this.networkId = networkId;
	}

	public Long getHttpTimeoutSeconds() {
		return httpTimeoutSeconds;
	}

	public void setHttpTimeoutSeconds(Long httpTimeoutSeconds) {
		this.httpTimeoutSeconds = httpTimeoutSeconds;
	}

	public String getContractAddr() {
		return contractAddr;
	}

	public void setContractAddr(String contractAddr) {
		this.contractAddr = contractAddr;
	}

	public String getGasPriceGwei() {
		return gasPriceGwei;
	}

	public void setGasPriceGwei(String gasPriceGwei) {
		this.gasPriceGwei = gasPriceGwei;
	}

	public String getGasLimitMax() {
		return gasLimitMax;
	}

	public void setGasLimitMax(String gasLimitMax) {
		this.gasLimitMax = gasLimitMax;
	}

	public String getGasLimitMid() {
		return gasLimitMid;
	}

	public void setGasLimitMid(String gasLimitMid) {
		this.gasLimitMid = gasLimitMid;
	}

	public String getGasLimitMin() {
		return gasLimitMin;
	}

	public void setGasLimitMin(String gasLimitMin) {
		this.gasLimitMin = gasLimitMin;
	}

}
