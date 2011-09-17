package org.lwap.plugin.webking;
/*
 * 支付业务
 * */
import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.Date;
import java.util.Iterator;
import uncertain.composite.CompositeMap;
import uncertain.proc.AbstractEntry;
import uncertain.proc.ProcedureRunner;
import com.kingdee.bos.ebservice.EBException;
import com.kingdee.bos.ebservice.EBHeader;
import com.kingdee.bos.ebservice.PayBody;
import com.kingdee.bos.ebservice.PayResponse;
import com.kingdee.bos.ebservice.PaymentDetail;
import com.kingdee.bos.ebservice.client.demo.junit.KingdeeEBException;
import com.kingdee.bos.ebservice.client.demo.utils.DES;
import com.kingdee.bos.ebservice.client.demo.utils.DateUtil;
import com.kingdee.bos.ebservice.client.demo.utils.Sequence;
import com.kingdee.bos.ebservice.client.hand.pay.ClientPayUtils;
import com.kingdee.bos.ebservice.client.hand.utils.EBHeaderUtils;

public class PayAction extends AbstractEntry {
	private static final PaymentDetail[][] PaymentDetail = null;
	public String accno;
	public String oppaccno;
	public String amount;
	public String currency;
	public String name;
	public String bank;
	public String address;
	public String detailbizno;
	public String desc;
	public String batch;
	public String usedesc;
	private ServiceSettings settings;
	
	public String getUsedesc() {
		return usedesc;
	}

	public void setUsedesc(String usedesc) {
		this.usedesc = usedesc;
	}

	public PayAction(ServiceSettings settings) {
		this.settings = settings;
	}

	public String getBatch() {
		return batch;
	}

	public void setBatch(String batch) {
		this.batch = batch;
	}

	public String getAccno() {
		return accno;
	}

	public void setAccno(String accno) {
		this.accno = accno;
	}

	public String getOppaccno() {
		return oppaccno;
	}

	public void setOppaccno(String oppaccno) {
		this.oppaccno = oppaccno;
	}

	public String getAmount() {
		return amount;
	}

	public void setAmount(String amount) {
		this.amount = amount;
	}

	public String getCurrency() {
		return currency;
	}

	public void setCurrency(String currency) {
		this.currency = currency;
	}

	public String getName() {
		return name;
	}

	public void setName(String name) {
		this.name = name;
	}

	public String getBank() {
		return bank;
	}

	public void setBank(String bank) {
		this.bank = bank;
	}

	public String getAddress() {
		return address;
	}

	public void setAddress(String address) {
		this.address = address;
	}

	public String getDetailbizno() {
		return detailbizno;
	}

	public void setDetailbizno(String detailbizno) {
		this.detailbizno = detailbizno;
	}

	public String getDesc() {
		return desc;
	}

	public void setDesc(String desc) {
		this.desc = desc;
	}

	public void run(ProcedureRunner runner) throws Exception {

		CompositeMap context = runner.getContext();
		CompositeMap result =  new CompositeMap("result");
		/*
		 *  让这段代码不报错误 把所有exception catch住，有利于页面的刷新
		 *   如果发生exception 
		 *   在数据上有yc_ebank_   ....._detail 体现为batch_id=-1;
		 * */
		try {
			CompositeMap cmlist = (CompositeMap) context.getObject(batch);
			String detailSeqID = Sequence.genSequence();
			int port = this.settings.getServicePORT();
			String ip = this.settings.getServiceIP();
			String path = "success";
			String currency_code = "";
			ClientPayUtils payUtils = new ClientPayUtils(ip, port, true);
			Iterator it = cmlist.getChildIterator();
			ArrayList<PaymentDetail> cl = new ArrayList();
			CompositeMap returnlist = new CompositeMap("returnlist1");
			String accnoheader = "";
			while (it.hasNext()) {

				CompositeMap cmrecord = (CompositeMap) it.next();
				String accNo = cmrecord.getString(this.getAccno());
				accnoheader = cmrecord.getString(this.getAccno());
				String oppAccNo = cmrecord.get(this.getOppaccno()).toString();
				String amount = cmrecord.get(this.getAmount()).toString();
				BigDecimal bd = new BigDecimal(amount);
				bd = bd.setScale(2, BigDecimal.ROUND_HALF_UP);
				amount = bd.toString();
				currency_code = cmrecord.get(this.getCurrency()).toString();

				// String name = "胡玉玲";
				String name = cmrecord.get(this.getName()).toString();
				boolean urgent = false;
				boolean toIndividual = false;
				// 同行支付
				String bank = cmrecord.get(this.getBank()).toString();
				String address = "";
				try {
					address = cmrecord.get(this.getAddress()).toString();
				} catch (NullPointerException ne) {

				}
				// String bank = "工商银行";
				// String address = "山东省的某个城市";
//				String useCn = "工资";
				String useCn=cmrecord.get(this.getUsedesc()).toString();
				String detailBizNo = cmrecord.get(this.getDetailbizno())
						.toString();
				cmrecord.get(this.getDesc()).toString();
				String detailSeqID1 = Sequence.genSequence();
				CompositeMap detail = new CompositeMap(detailSeqID1);
				detail.put(this.getDetailbizno(), detailBizNo);
				returnlist.addChild(detail);
				PaymentDetail pd = createPaymentDetail(detailSeqID1,
						detailBizNo, oppAccNo, name, toIndividual, bank,
						address, amount, "-1", useCn, urgent, desc);
				cl.add(pd);

			}
			PaymentDetail[] a = new PaymentDetail[cl.size()];
			PaymentDetail[] pdarray = (PaymentDetail[]) cl.toArray(a);

			EBHeader header = EBHeaderUtils.createHeader("MBTS", "MBTS6.0",
					"request", "pay", "pay", "pay", accnoheader, currency_code,
					DateUtil.formatDateTime(new Date()));

			PayResponse pay = payUtils.callWS(header, detailSeqID, pdarray);
			EBException ebe = pay.getException();
			if (null != ebe) {
				throw new KingdeeEBException(ebe.getMessage());
			} else {
				PayBody detailBody = pay.getBody();
				PaymentDetail[] paydetail = detailBody.getDetails();
				for (int i = 0; i < paydetail.length; i++) {
					CompositeMap detail = (CompositeMap) returnlist
							.getObject(paydetail[i].getDetailSeqID());
					String bankstatus = "undefined";
					try {
						bankstatus = paydetail[i].getBankStatus();
						int begin = bankstatus.indexOf('>', 1);
						int end = bankstatus.indexOf('<', 2);
						bankstatus = bankstatus.substring(begin + 1, end);
					} catch (Exception e) {
						bankstatus = "undefined";
					}
					detail.put("EBSTATUS", bankstatus);
					detail.put("EBSTATUSMSG", paydetail[i].getEbStatusMsg()
							.toString());
					detail.put("MSTATUS", paydetail[i].getEbStatus());
					detail.put("MSTATUSMSG", paydetail[i].getBankStatusMsg());
					detail.put("BATCH_ID", detailBody.getBatchSeqID());
					detail.put("BATCH_NO", detailBody.getBatchBizNo());
					detail.put("DETAILSEQID", paydetail[i].getDetailSeqID());
				}
			}
			CompositeMap cm = new CompositeMap("returnlist");
			Iterator its = returnlist.getChildIterator();
			while (its.hasNext()) {
				CompositeMap copy = (CompositeMap) its.next();
				CompositeMap record = new CompositeMap("record");
				record.copy(copy);
				cm.addChild(record);
			}
			context.addChild(cm);
			
			result.put("result", "success");
		} catch (Exception e) {
			result.put("result", e.getMessage());
		}
		context.addChild(result);
	}

	/*组装行数据
	 * CPIC0001 是写死的，可以用STATIC 代替，后期没改，如果程序效率慢，可修改
	 * */
	protected PaymentDetail createPaymentDetail(String detailSeqID,
			String detailBizNo, String acc, String name, boolean toIndividual,
			String bank, String address, String amount, String useCode,
			String useCN, boolean urgent, String desc) {
		PaymentDetail detail = new PaymentDetail();
		detail.setDetailSeqID(detailSeqID);// Sequence.genSequence()
		detail.setDetailBizNo(detailBizNo);
		detail.setAmount(amount);
		detail.setPayeeAccNo(acc);
		detail.setPayeeAccName(name);
		detail.setPayeeBankName(bank);
		detail.setDesc(desc);
		if (toIndividual) {
			detail.setPayeeType("individual");
		} else {
			detail.setPayeeType("company");
		}
		detail.setUseCode(useCode);
		detail.setUse(useCN);
		detail.setPayeeBankAddr(address);
		detail.setUrgent("" + urgent);
		String keyCode = "CPIC0001";
		;
		String des = DES.des_encrypt(keyCode, detail.getPayeeAccNo()
				+ detail.getAmount());
		detail.setVerifyField(des);

		return detail;
	}

}
