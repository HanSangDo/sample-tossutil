package cmmn.vo;

import java.io.Serializable;

public class TossPayVO implements Serializable {

	private static final long serialVersionUID = 1L;

	private String siteCd;

	/** 결제키 */
	private String paymentKey;

	/** 주문번호 */
	private String orderId;

	/** 결제금액 */
	private String amount;

	private String cancelReason;

	public String getSiteCd() {

		return siteCd;
	}

	public void setSiteCd(String siteCd) {

		this.siteCd = siteCd;
	}

	public String getPaymentKey() {

		return paymentKey;
	}

	public void setPaymentKey(String paymentKey) {

		this.paymentKey = paymentKey;
	}

	public String getOrderId() {

		return orderId;
	}

	public void setOrderId(String orderId) {

		this.orderId = orderId;
	}

	public String getAmount() {

		return amount;
	}

	public void setAmount(String amount) {

		this.amount = amount;
	}

	public String getCancelReason() {

		return cancelReason;
	}

	public void setCancelReason(String cancelReason) {

		this.cancelReason = cancelReason;
	}

}
