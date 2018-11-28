package io.hpb.pay.controller;

import java.math.BigInteger;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.TimeUnit;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import io.hpb.pay.configure.Web3Properties;
import io.hpb.pay.contract.HpbPay;
import io.hpb.web3.abi.datatypes.Address;
import io.hpb.web3.abi.datatypes.Utf8String;
import io.hpb.web3.abi.datatypes.generated.Uint256;
import io.hpb.web3.crypto.Credentials;
import io.hpb.web3.crypto.WalletUtils;
import io.hpb.web3.protocol.admin.Admin;
import io.hpb.web3.protocol.core.DefaultBlockParameterName;
import io.hpb.web3.protocol.core.methods.response.HpbBlockNumber;
import io.hpb.web3.protocol.core.methods.response.HpbGetBalance;
import io.hpb.web3.protocol.core.methods.response.HpbGetTransactionCount;
import io.hpb.web3.protocol.core.methods.response.HpbGetTransactionReceipt;
import io.hpb.web3.protocol.core.methods.response.TransactionReceipt;
import io.hpb.web3.tx.ChainId;
import io.hpb.web3.tx.RawTransactionManager;
import io.hpb.web3.utils.Convert;
import io.swagger.annotations.ApiOperation;

@RestController
@RequestMapping("/")
public class HpbPayController{
	private static Log log = LogFactory.getLog(HpbPayController.class);
	@Autowired
	private Admin admin;
	@Autowired
    private Web3Properties web3Properties;
	@ApiOperation(value="通过根据交易hash查询交易收据",notes = "过根据交易hash查询交易收据"
			+ " reqStrList ["
			+ "\n参数1：交易hash"
			+ "\n]")
	@PostMapping("/QueryByHash")
	public List<Object> QueryByHash(@RequestBody List<String> reqStrList)throws Exception{
		List<Object> list=new ArrayList<Object>();
		if(reqStrList!=null&&reqStrList.size()>0) {
			String transactionHash = reqStrList.get(0);
			HpbGetTransactionReceipt receipt = admin.hpbGetTransactionReceipt(transactionHash).send();
			if(!receipt.hasError()) {
				TransactionReceipt transactionReceipt = receipt.getResult();
				if(transactionReceipt.isStatusOK()) {
					list.add(transactionReceipt);
				}
			}
		}
		return list;
	}
	@ApiOperation(value="获得当前区块号",notes = "获得当前区块号")
	@PostMapping("/getCurrentBlock")
	public List<Object> getCurrentBlock()throws Exception{
		List<Object> list=new ArrayList<Object>();
		HpbBlockNumber blockNumber = admin.hpbBlockNumber().sendAsync().
				get(web3Properties.getHttpTimeoutSeconds(), TimeUnit.SECONDS);
		list.add(blockNumber);
		return list;
	}
	@ApiOperation(value="获得当前账户的Nonce",notes = "获得当前账户的Nonce"
			+ " reqStrList ["
			+ "\n参数1：账户地址;"
			+ "\n]")
	@PostMapping("/getCurrentNonce")
	public List<Object> getCurrentNonce(@RequestBody List<String> reqStrList)throws Exception{
		List<Object> list=new ArrayList<Object>();
		if(reqStrList!=null&&reqStrList.size()>0) {
			String address =reqStrList.get(0);
			HpbGetTransactionCount transactionCount = admin.hpbGetTransactionCount(address, 
					DefaultBlockParameterName.PENDING).sendAsync().
					get(web3Properties.getHttpTimeoutSeconds(), TimeUnit.SECONDS);
			BigInteger nonce = transactionCount.getTransactionCount();
			log.info(nonce);
			list.add(nonce);
		}
		return list;
	}
	@ApiOperation(value="获得当前账户的余额",notes = "获得当前账户的余额"
			+ " reqStrList ["
			+ "\n参数1：账户地址;"
			+ "\n]")
	@PostMapping("/getBalance")
	public List<Object> getBalance(@RequestBody List<String> reqStrList)throws Exception{
		List<Object> list=new ArrayList<Object>();
		if(reqStrList!=null&&reqStrList.size()>0) {
			String address =reqStrList.get(0);
			HpbGetBalance balance = admin.hpbGetBalance(address, DefaultBlockParameterName.LATEST).sendAsync().
					get(web3Properties.getHttpTimeoutSeconds(), TimeUnit.SECONDS);
			log.info(balance);
			list.add(balance);
		}
		return list;
	}
	
	@ApiOperation(value="商户更新自己的公钥和描述信息",notes = "商户更新自己的公钥和描述信息"
			+ " reqStrList ["
			+ "\n参数1：商户公钥； "
			+ "\n参数2：商户描述； "
			+ "\n参数3：账户keystore文件路径; "
			+ "\n参数4：密码"
			+ "\n]")
	@PostMapping("/updateMerchant")
	public List<Object> updateMerchant(@RequestBody List<String> reqStrList)throws Exception{
		List<Object> list=new ArrayList<Object>();
		if(reqStrList!=null&&reqStrList.size()>4) {
			String publicKey =reqStrList.get(0);
			String desc =reqStrList.get(1);
			String keystore =reqStrList.get(2);
			String password =reqStrList.get(3);
			Credentials credentials = WalletUtils.loadCredentials(password, keystore);
			RawTransactionManager transactionManager=new RawTransactionManager(admin, credentials, ChainId.MAINNET);
			
			String gasPriceGwei = web3Properties.getGasPriceGwei();
			String gasLimitMin = web3Properties.getGasLimitMin();
			HpbPay hpbPay = HpbPay.load(
					web3Properties.getContractAddr(), 
					admin, 
					transactionManager, 
					Convert.toWei(gasPriceGwei, Convert.Unit.GWEI).toBigInteger(),
					new BigInteger(gasLimitMin)
			);
			TransactionReceipt receipt = hpbPay.updateMerchant( new Utf8String(publicKey), new Utf8String(desc)).
					sendAsync().get(web3Properties.getHttpTimeoutSeconds(), TimeUnit.SECONDS);
			list.add(receipt);
		}
		return list;
	}
	@ApiOperation(value="商户本人查看自己提供的公钥",notes = "商户本人查看自己提供的公钥"
			+ " reqStrList ["
			+ "\n参数1：账户keystore文件路径; "
			+ "\n参数2：密码"
			+ "\n]")
	@PostMapping("/getMerchantPublicKey")
	public List<Object> getMerchantPublicKey(@RequestBody List<String> reqStrList)throws Exception{
		List<Object> list=new ArrayList<Object>();
		if(reqStrList!=null&&reqStrList.size()>1) {
			String keystore =reqStrList.get(0);
			String password =reqStrList.get(1);
			Credentials credentials = WalletUtils.loadCredentials(password, keystore);
			RawTransactionManager transactionManager=new RawTransactionManager(admin, credentials, ChainId.MAINNET);
			
			String gasPriceGwei = web3Properties.getGasPriceGwei();
			String gasLimitMin = web3Properties.getGasLimitMin();
			HpbPay hpbPay = HpbPay.load(
					web3Properties.getContractAddr(), 
					admin, 
					transactionManager, 
					Convert.toWei(gasPriceGwei, Convert.Unit.GWEI).toBigInteger(),
					new BigInteger(gasLimitMin)
			);
			Utf8String publicKey = hpbPay.getMerchantPublicKey().sendAsync().get(
					web3Properties.getHttpTimeoutSeconds(), TimeUnit.SECONDS);
			list.add(publicKey.getValue());
		}
		return list;
	}

	@ApiOperation(value="由商户生成订单(并指定特定收款地址)",notes = "由商户生成订单(并指定特定收款地址)"
			+ " reqStrList ["
			+ "\n参数1：支付方付款账户地址； "
			+ "\n参数2：商户方收款账户地址； "
			+ "\n参数3： 金额(单位为HPB，支持18位小数)； "
			+ "\n参数4：商户订单业务ID(唯一)； "
			+ "\n参数5：返回商户APP的URL； "
			+ "\n参数6：商户订单业务简述； "
			+ "\n参数7：商户账户keystore文件路径; "
			+ "\n参数8：商户账户密码"
			+ "\n]")
	@PostMapping("/generateOrderByMerchantWithPayee")
	public List<Object> generateOrderByMerchantWithPayee(@RequestBody List<String> reqStrList)throws Exception{
		List<Object> list=new ArrayList<Object>();
		if(reqStrList!=null&&reqStrList.size()>7) {
			String from=reqStrList.get(0);
			String to=reqStrList.get(1);
			String value=reqStrList.get(2);
			String orderId=reqStrList.get(3);
			String backUrl=reqStrList.get(4);
			String desc=reqStrList.get(5);
			String keystore =reqStrList.get(6);
			String password =reqStrList.get(7);
			Credentials credentials = WalletUtils.loadCredentials(password, keystore);
			RawTransactionManager transactionManager=new RawTransactionManager(admin, credentials, ChainId.MAINNET);
			
			String gasPriceGwei = web3Properties.getGasPriceGwei();
			String gasLimitMin = web3Properties.getGasLimitMin();
			HpbPay hpbPay = HpbPay.load(
					web3Properties.getContractAddr(), 
					admin, 
					transactionManager, 
					Convert.toWei(gasPriceGwei, Convert.Unit.GWEI).toBigInteger(),
					new BigInteger(gasLimitMin)
			);
			TransactionReceipt receipt = hpbPay.generateOrderByMerchantWithPayee(
					new Address(from),
					new Address(to),
					new Uint256(Convert.toWei(value, Convert.Unit.HPB).toBigInteger()), 
					new Utf8String(orderId), 
					new Utf8String(backUrl), 
					new Utf8String(desc)).sendAsync().get(
							web3Properties.getHttpTimeoutSeconds(), TimeUnit.SECONDS);
			list.add(receipt);
		}
		return list;
	}
	@ApiOperation(value="由商户生成订单(默认指定商户账户地址为收款地址)",notes = "由商户生成订单(默认指定商户账户地址为收款地址)"
			+ " reqStrList ["
			+ "\n参数1：支付方付款账户地址； "
			+ "\n参数2： 金额(单位为HPB，支持18位小数)； "
			+ "\n参数3：商户订单业务ID(唯一)； "
			+ "\n参数4：返回商户APP的URL； "
			+ "\n参数5：商户订单业务简述； "
			+ "\n参数6：商户账户keystore文件路径; "
			+ "\n参数7：商户账户密码"
			+ "\n]")
	@PostMapping("/generateOrderByMerchant")
	public List<Object> generateOrderByMerchant(@RequestBody List<String> reqStrList)throws Exception{
		List<Object> list=new ArrayList<Object>();
		if(reqStrList!=null&&reqStrList.size()>6) {
			String from=reqStrList.get(0);
			String value=reqStrList.get(1);
			String orderId=reqStrList.get(2);
			String backUrl=reqStrList.get(3);
			String desc=reqStrList.get(4);
			String keystore =reqStrList.get(5);
			String password =reqStrList.get(6);
			Credentials credentials = WalletUtils.loadCredentials(password, keystore);
			RawTransactionManager transactionManager=new RawTransactionManager(admin, credentials, ChainId.MAINNET);
			
			String gasPriceGwei = web3Properties.getGasPriceGwei();
			String gasLimitMin = web3Properties.getGasLimitMin();
			HpbPay hpbPay = HpbPay.load(
					web3Properties.getContractAddr(), 
					admin, 
					transactionManager, 
					Convert.toWei(gasPriceGwei, Convert.Unit.GWEI).toBigInteger(),
					new BigInteger(gasLimitMin)
			);
			TransactionReceipt receipt = hpbPay.generateOrderByMerchant(
					new Address(from),
					new Uint256(Convert.toWei(value, Convert.Unit.HPB).toBigInteger()), 
					new Utf8String(orderId), 
					new Utf8String(backUrl), 
					new Utf8String(desc)).sendAsync().get(
							web3Properties.getHttpTimeoutSeconds(), TimeUnit.SECONDS);
			list.add(receipt);
		}
		return list;
	}
	
}