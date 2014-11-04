package com.xeiam.xchange.justcoin;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;

import com.xeiam.xchange.currency.CurrencyPair;
import com.xeiam.xchange.dto.Order.OrderType;
import com.xeiam.xchange.dto.account.AccountInfo;
import com.xeiam.xchange.dto.marketdata.OrderBook;
import com.xeiam.xchange.dto.marketdata.Ticker;
import com.xeiam.xchange.dto.marketdata.Trade;
import com.xeiam.xchange.dto.marketdata.Trades;
import com.xeiam.xchange.dto.marketdata.Trades.TradeSortType;
import com.xeiam.xchange.dto.trade.LimitOrder;
import com.xeiam.xchange.dto.trade.OpenOrders;
import com.xeiam.xchange.dto.trade.UserTrade;
import com.xeiam.xchange.dto.trade.UserTrades;
import com.xeiam.xchange.dto.trade.Wallet;
import com.xeiam.xchange.justcoin.dto.account.JustcoinBalance;
import com.xeiam.xchange.justcoin.dto.marketdata.JustcoinDepth;
import com.xeiam.xchange.justcoin.dto.marketdata.JustcoinPublicTrade;
import com.xeiam.xchange.justcoin.dto.marketdata.JustcoinTicker;
import com.xeiam.xchange.justcoin.dto.trade.out.JustcoinOrder;
import com.xeiam.xchange.justcoin.dto.trade.out.JustcoinTrade;

/**
 * jamespedwards42
 */
public final class JustcoinAdapters {

  private JustcoinAdapters() {

  }

  public static List<LimitOrder> adaptOrders(final List<List<BigDecimal>> justcoinOrders, final CurrencyPair currencyPair, final OrderType orderType) {

    final List<LimitOrder> limitOrders = new ArrayList<LimitOrder>();
    for (final List<BigDecimal> justcoinOrder : justcoinOrders) {
      limitOrders.add(adaptOrder(justcoinOrder.get(1), justcoinOrder.get(0), currencyPair, orderType));
    }

    return limitOrders;
  }

  public static LimitOrder adaptOrder(final BigDecimal amount, final BigDecimal price, final CurrencyPair currencyPair, final OrderType orderType) {

    return new LimitOrder(orderType, amount, currencyPair, null, null, price);
  }

  public static Ticker adaptTicker(final List<JustcoinTicker> justcoinTickers, final CurrencyPair currencyPair) {

    for (final JustcoinTicker justcointTicker : justcoinTickers) {
      if (justcointTicker.getId().equals(JustcoinUtils.getApiMarket(currencyPair.baseSymbol, currencyPair.counterSymbol))) {
        return new Ticker.Builder().currencyPair(currencyPair).last(justcointTicker.getLast()).bid(justcointTicker.getBid()).ask(justcointTicker.getAsk()).high(
                justcointTicker.getHigh()).low(justcointTicker.getLow()).volume(justcointTicker.getVolume()).build();
      }
    }

    return null;
  }

  public static AccountInfo adaptAccountInfo(final String username, final JustcoinBalance[] justcoinBalances) {

    final List<Wallet> wallets = new ArrayList<Wallet>();
    for (final JustcoinBalance balanceForCurrency : justcoinBalances) {
      wallets.add(adaptWallet(balanceForCurrency));
    }

    return new AccountInfo(username, wallets);
  }

  public static Wallet adaptWallet(final JustcoinBalance justcoinBalance) {

    final String currency = justcoinBalance.getCurrency();
    final BigDecimal balance = justcoinBalance.getBalance();
    return new Wallet(currency, balance);
  }

  public static OpenOrders adaptOpenOrders(final JustcoinOrder[] justoinOrders) {

    final List<LimitOrder> openOrders = new ArrayList<LimitOrder>();
    for (final JustcoinOrder justcoinOrder : justoinOrders) {
      openOrders.add(adaptLimitOrder(justcoinOrder));
    }

    return new OpenOrders(openOrders);
  }

  public static OrderBook adaptOrderBook(final CurrencyPair currencyPair, final JustcoinDepth justcoinDepth) {

    final List<LimitOrder> asks = JustcoinAdapters.adaptOrders(justcoinDepth.getAsks(), currencyPair, OrderType.ASK);
    final List<LimitOrder> bids = JustcoinAdapters.adaptOrders(justcoinDepth.getBids(), currencyPair, OrderType.BID);

    return new OrderBook(null, asks, bids);
  }

  public static CurrencyPair adaptCurrencyPair(final String justcoinMarket) {

    return new CurrencyPair(justcoinMarket.substring(0, 3), justcoinMarket.substring(3));
  }

  public static LimitOrder adaptLimitOrder(final JustcoinOrder justcoinOrder) {

    return new LimitOrder(OrderType.valueOf(justcoinOrder.getType().toUpperCase()), justcoinOrder.getRemaining(), adaptCurrencyPair(justcoinOrder.getMarket()), justcoinOrder.getId(), justcoinOrder
        .getCreatedAt(), justcoinOrder.getPrice());
  }

  public static UserTrades adaptTrades(final JustcoinTrade[] justoinOrders) {

    final List<UserTrade> openOrders = new ArrayList<UserTrade>();
    for (final JustcoinTrade justcoinOrder : justoinOrders) {
      openOrders.add(adaptTrade(justcoinOrder));
    }

    return new UserTrades(openOrders, TradeSortType.SortByID);
  }

  public static UserTrade adaptTrade(final JustcoinTrade justcoinTrade) {

    return new UserTrade(OrderType.valueOf(justcoinTrade.getType().toUpperCase()), justcoinTrade.getAmount(), adaptCurrencyPair(justcoinTrade.getMarket()), justcoinTrade.getAveragePrice(), justcoinTrade
        .getCreatedAt(), justcoinTrade.getId(), null, null, null);
  }

  public static Trades adaptPublicTrades(final CurrencyPair currencyPair, final List<JustcoinPublicTrade> justcoinTrades) {

    final List<Trade> trades = new ArrayList<Trade>();
    long lastTradeId = 0;
    for (final JustcoinPublicTrade trade : justcoinTrades) {
      long tradeId = Long.valueOf(trade.getTid());
      if (tradeId > lastTradeId)
        lastTradeId = tradeId;
      trades.add(adaptPublicTrade(currencyPair, trade));
    }

    return new Trades(trades, lastTradeId, TradeSortType.SortByID);
  }

  public static Trade adaptPublicTrade(final CurrencyPair currencyPair, final JustcoinPublicTrade justcoinTrade) {

    return new Trade(null, justcoinTrade.getAmount(), currencyPair, justcoinTrade.getPrice(), new Date(justcoinTrade.getDate() * 1000), justcoinTrade.getTid());
  }
}
