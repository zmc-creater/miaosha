package com.mc.miaosha.dataobject;

public class StockLogDO {
    /**
     *
     * This field was generated by MyBatis Generator.
     * This field corresponds to the database column stock_log.stock_log_id
     *
     * @mbg.generated Sat Sep 18 17:57:43 CST 2021
     */
    private String stockLogId;

    /**
     *
     * This field was generated by MyBatis Generator.
     * This field corresponds to the database column stock_log.item_id
     *
     * @mbg.generated Sat Sep 18 17:57:43 CST 2021
     */
    private Integer itemId;

    /**
     *
     * This field was generated by MyBatis Generator.
     * This field corresponds to the database column stock_log.amount
     *
     * @mbg.generated Sat Sep 18 17:57:43 CST 2021
     */
    private Integer amount;

    /**
     *
     * This field was generated by MyBatis Generator.
     * This field corresponds to the database column stock_log.status
     *
     * @mbg.generated Sat Sep 18 17:57:43 CST 2021
     */
    // 1 初始状态；2 成功； 3 失败
    private Integer status;

    /**
     * This method was generated by MyBatis Generator.
     * This method returns the value of the database column stock_log.stock_log_id
     *
     * @return the value of stock_log.stock_log_id
     *
     * @mbg.generated Sat Sep 18 17:57:43 CST 2021
     */
    public String getStockLogId() {
        return stockLogId;
    }

    /**
     * This method was generated by MyBatis Generator.
     * This method sets the value of the database column stock_log.stock_log_id
     *
     * @param stockLogId the value for stock_log.stock_log_id
     *
     * @mbg.generated Sat Sep 18 17:57:43 CST 2021
     */
    public void setStockLogId(String stockLogId) {
        this.stockLogId = stockLogId;
    }

    /**
     * This method was generated by MyBatis Generator.
     * This method returns the value of the database column stock_log.item_id
     *
     * @return the value of stock_log.item_id
     *
     * @mbg.generated Sat Sep 18 17:57:43 CST 2021
     */
    public Integer getItemId() {
        return itemId;
    }

    /**
     * This method was generated by MyBatis Generator.
     * This method sets the value of the database column stock_log.item_id
     *
     * @param itemId the value for stock_log.item_id
     *
     * @mbg.generated Sat Sep 18 17:57:43 CST 2021
     */
    public void setItemId(Integer itemId) {
        this.itemId = itemId;
    }

    /**
     * This method was generated by MyBatis Generator.
     * This method returns the value of the database column stock_log.amount
     *
     * @return the value of stock_log.amount
     *
     * @mbg.generated Sat Sep 18 17:57:43 CST 2021
     */
    public Integer getAmount() {
        return amount;
    }

    /**
     * This method was generated by MyBatis Generator.
     * This method sets the value of the database column stock_log.amount
     *
     * @param amount the value for stock_log.amount
     *
     * @mbg.generated Sat Sep 18 17:57:43 CST 2021
     */
    public void setAmount(Integer amount) {
        this.amount = amount;
    }

    /**
     * This method was generated by MyBatis Generator.
     * This method returns the value of the database column stock_log.status
     *
     * @return the value of stock_log.status
     *
     * @mbg.generated Sat Sep 18 17:57:43 CST 2021
     */
    public Integer getStatus() {
        return status;
    }

    /**
     * This method was generated by MyBatis Generator.
     * This method sets the value of the database column stock_log.status
     *
     * @param status the value for stock_log.status
     *
     * @mbg.generated Sat Sep 18 17:57:43 CST 2021
     */
    public void setStatus(Integer status) {
        this.status = status;
    }
}