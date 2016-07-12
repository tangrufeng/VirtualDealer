package org.tom.vd;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.apache.commons.lang.StringUtils;
import org.apache.log4j.Logger;
import org.tom.vd.bean.GameRoundInfo;
import org.tom.vd.bean.ProgessBean;
import org.tom.vd.cache.CardCache;
import org.tom.vd.config.Config;
import org.tom.vd.db.DBHelper;
import org.tom.vd.mina.ApCommand;
import org.tom.vd.mina.MinaGameInterface;
import org.tom.vd.rmtp.FlowPusher;
import org.tom.vd.rmtp.OnProgressListener;

import com.effecia.mina.MinaCommandPo;

import net.sf.json.JSONArray;
import net.sf.json.JSONObject;

/**
 * <p>
 * Title: VirtualDealer.java
 * </p>
 * <p>
 * Description:
 * </p>
 * <p>
 * Copyright: Copyright (c) 2014
 * </p>
 * <p>
 * Company: jc-yt.com
 * </p>
 * 
 * @author tang
 * @date 2016-1-10 7:05:17
 * @version 1.0
 * 
 */
public class VirtualDealer implements OnProgressListener {

	private static final Logger logger = Logger.getLogger(VirtualDealer.class);

	private static Pattern pattern = Pattern.compile("\\d{2}:\\d{2}:\\d{2}");

	private static final String TIMELINE = "time=";

	private static final SimpleDateFormat dateformat = new SimpleDateFormat("00:mm:ss");

	private Boolean newProgress = false;

	private List<String> hisProList = new ArrayList<String>(); // 存储某局牌的历史发牌信息

	private GameRoundInfo lastGame, currentGame, designatedGame;

	private Integer gameId = 0;

	private int countDown = 0; // 下注等待时间

	Map<String, LinkedList<ProgessBean>> progressCache = new HashMap<String, LinkedList<ProgessBean>>();

	private final static VirtualDealer vd = new VirtualDealer();

	public static VirtualDealer getVD() {
		return vd;
	}

	private VirtualDealer() {

	}

	public void start() {

		currentGame = CardCache.getCache().poll();
		if (designatedGame != null) {
			designatedGame.setShoeid(currentGame.getShoeid());
			currentGame = designatedGame;
			designatedGame = null;
		}

		logger.info("begin play:" + currentGame);
		if (currentGame != null) {
			pushStreamToRed5(currentGame, currentGame.getId() + "");
			parseBJLProgessToCache(currentGame);
			lastGame = currentGame; // 保留上一盘数据，以便于判断是否换靴
		}

	}

	public void pushStreamToRed5(GameRoundInfo game, String id) {
		FlowPusher pusher = new FlowPusher(game, Config.getCfg().getString("red5host"));
		pusher.setOnProgressListener(this);
		new Thread(pusher).start();
	}

	public void pushToMina(GameRoundInfo game, String timeline) {
		synchronized (progressCache) {
			LinkedList<ProgessBean> pbList = progressCache.remove(timeline);
			if (pbList == null) {
				return;
			}
			for (ProgessBean pb : pbList) {
				logger.info(pb);
				if ("begin".equals(pb.getStatus())) { // 开始下注
					sendStartCommond(game);
				} else if ("dealing_wait".equals(pb.getStatus())) { // 停止下注
					sendDealingWaitCommond(game);
				} else if ("deal".equals(pb.getStatus())) { // 发牌
					sendDealCommond(game, pb);
				} else if ("end".equals(pb.getStatus())) { // 结束
					sendEndCommand(game);
					if ("1".equals(game.getChargeShoe())) { // 换靴
						sendChangeShoeidCommand(lastGame);
					}
				} else if ("interrupt".equals(pb.getStatus())) { // 有遮挡
					sendInterruptCommand(game);
				} else if ("nonterrupt".equals(pb.getStatus())) { // 无遮挡
					sendNoInterruptCommand(game);
				}
			}
		}
	}

	/**
	 * @param game
	 */
	private void sendChangeShoeidCommand(GameRoundInfo game) {
		MinaCommandPo mcp = new MinaCommandPo();
		mcp.setRequestType("REQUEST");
		mcp.setModuleType("AP");
		mcp.setCommandType(ApCommand.END_SHUFFLE.name());
		Map map = mcp.getParameter();
		map.put("tableId", game.getRoomId());//
		map.put("shoeId", game.getShoeid() + "");
		synchronized (gameId) {
			gameId = 0; // 换靴时，gameId要清零
		}
		logger.info(mcp);
		MinaGameInterface.ioSession.write(mcp);
	}

	/**
	 * @param game
	 */
	private void sendInterruptCommand(GameRoundInfo game) {
		MinaCommandPo mcp = new MinaCommandPo();
		mcp.setRequestType("REQUEST");
		mcp.setModuleType("AP");
		mcp.setCommandType(ApCommand.INTERRUPT.toString());
		Map map = mcp.getParameter();
		map.put("tableId", game.getRoomId());//
		logger.info(mcp);
		MinaGameInterface.ioSession.write(mcp);
	}

	/**
	 * @param game
	 */
	private void sendNoInterruptCommand(GameRoundInfo game) {
		MinaCommandPo mcp = new MinaCommandPo();
		mcp.setRequestType("REQUEST");
		mcp.setModuleType("AP");
		mcp.setCommandType(ApCommand.NOINTERRUPT.toString());
		Map map = mcp.getParameter();
		map.put("tableId", game.getRoomId());//
		logger.info(mcp);
		MinaGameInterface.ioSession.write(mcp);
	}

	/**
	 * @param game
	 */
	private void sendEndCommand(GameRoundInfo game) {
		MinaCommandPo mcp = new MinaCommandPo();
		mcp.setRequestType("REQUEST");
		mcp.setModuleType("AP");
		mcp.setCommandType(ApCommand.END.toString());
		Map map = mcp.getParameter();
		map.put("tableId", game.getRoomId());//
		map.put("shoeId", game.getShoeid() + "");
		map.put("gameId", gameId);
		map.put("cardInfo", game.getCardInfo());
		map.put("gameRoundInfoId", game.getId());
		logger.info(mcp);
		MinaGameInterface.ioSession.write(mcp);
	}

	/**
	 * @param game
	 * @param pb
	 */
	private void sendDealCommond(GameRoundInfo game, ProgessBean pb) {
		if (newProgress) {
			hisProList.clear(); // 当一局新开始时，先清除历史纪录
			newProgress = false;
		}
		hisProList.add(JsonUtil.object2Json(pb));
		MinaCommandPo mcp = new MinaCommandPo();
		mcp.setRequestType("REQUEST");
		mcp.setModuleType("AP");
		mcp.setCommandType(ApCommand.DEAL.toString());
		Map map = mcp.getParameter();
		map.put("tableId", game.getRoomId());//
		map.put("shoeId", game.getShoeid() + "");
		map.put("gameId", gameId);
		map.put("bnum", pb.getBnum());
		map.put("fnum", pb.getFnum());
		map.put("bcolor", pb.getBcolor());
		map.put("fcolor", pb.getFcolor());
		map.put("gameRoundInfoId", game.getId());
		map.put("history", hisProList);
		logger.info(mcp);
		MinaGameInterface.ioSession.write(mcp);
	}

	/**
	 * @param game
	 */
	private void sendDealingWaitCommond(GameRoundInfo game) {
		MinaCommandPo mcp = new MinaCommandPo();
		mcp.setRequestType("REQUEST");
		mcp.setModuleType("AP");
		mcp.setCommandType(ApCommand.DEALING_WAIT.toString());
		Map map = mcp.getParameter();
		map.put("tableId", game.getRoomId());//
		map.put("shoeId", game.getShoeid() + "");
		map.put("gameId", gameId);
		map.put("gameRoundInfoId", game.getId());
		logger.info(mcp);
		MinaGameInterface.ioSession.write(mcp);
	}

	/**
	 * @param game
	 */
	private void sendStartCommond(GameRoundInfo game) {
		MinaCommandPo mcp = new MinaCommandPo();
		mcp.setRequestType("REQUEST");
		mcp.setModuleType("AP");
		mcp.setCommandType(ApCommand.START.toString());
		Map map = mcp.getParameter();
		map.put("tableId", game.getRoomId());//
		map.put("shoeId", game.getShoeid() + "");
		map.put("gameId", ++gameId);
		map.put("countDown", countDown);// 发送投注倒计时
		map.put("gameRoundInfoId", game.getId());
		logger.info(mcp);
		MinaGameInterface.ioSession.write(mcp);
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see
	 * org.tom.vd.rmtp.OnProgressListener#onProgressListener(java.lang.String)
	 */
	@Override
	public void onProgressListener(GameRoundInfo game, String commondOutput) {
		if (StringUtils.isNotEmpty(commondOutput)) {
			if (commondOutput.startsWith("frame=")) {
				if ("win".equals(Config.getCfg().getString("os.version"))) {
					Matcher m = pattern.matcher(commondOutput);
					if (m.find()) {
						String timeline = m.group();
						pushToMina(game, timeline);
					}
				} else {
					String timeline = parseTimeLine(commondOutput);
					logger.debug("timeline===>" + timeline);
					pushToMina(game, timeline);
				}
			}
		}
	}

	/**
	 * 
	 * @param str
	 * @return
	 */
	private String parseTimeLine(String str) {
		int index = str.indexOf(TIMELINE);
		if (index != -1) {
			int endIndex = str.indexOf(" ", index);
			String temp = str.substring(index + TIMELINE.length(), endIndex);
			temp = temp.substring(0, temp.indexOf("."));
			long longTmp = Long.parseLong(temp) * 1000;
			return dateformat.format(new Date(longTmp));
		}
		return "";
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see
	 * org.tom.vd.rmtp.OnProgressListener#onProgressStateChangedListener(boolean
	 * )
	 */
	@Override
	public void onProgressStateChangedListener(boolean isInProgress) {
		logger.debug("onProgressStateChangedListener===>" + isInProgress);
		if (!isInProgress) {
			start();
		}
	}

	private void parseBJLProgessToCache(GameRoundInfo g) {
		String betStartTime = "";
		String betEndTime = "";
		progressCache.clear();
		newProgress = true;
		JSONArray jr = JSONArray.fromObject(g.getCardInfo());
		for (int i = 0; i < jr.size(); i++) {
			JSONObject jo = jr.getJSONObject(i);
			ProgessBean bean = new ProgessBean();
			bean.setTimeline(jo.getString("timeline"));
			bean.setStatus(jo.getString("event"));
			if (jo.containsKey("bcolor"))
				bean.setBcolor(jo.getInt("bcolor"));
			if (jo.containsKey("bnum"))
				bean.setBnum(jo.getInt("bnum"));
			if (jo.containsKey("fcolor"))
				bean.setFcolor(jo.getInt("fcolor"));
			if (jo.containsKey("fnum"))
				bean.setFnum(jo.getInt("fnum"));
			if (jo.containsKey("nextColor"))
				bean.setNextColor(jo.getInt("nextColor"));
			if (jo.containsKey("nextDealer"))
				bean.setNextDealer(jo.getInt("nextDealer"));
			LinkedList<ProgessBean> list = progressCache.get(bean.getTimeline());
			if (list == null) {
				list = new LinkedList<ProgessBean>();
				progressCache.put(bean.getTimeline(), list);
			}
			if ("begin".equalsIgnoreCase(bean.getStatus())) {
				betStartTime = bean.getTimeline();
			} else if ("dealing_wait".equalsIgnoreCase(bean.getStatus())) {
				betEndTime = bean.getTimeline();
			}
			list.add(bean);
		}

		// 计算投注等待倒计时
		if (StringUtils.isNotEmpty(betStartTime) && StringUtils.isNotEmpty(betEndTime)) {
			try {
				long t1 = dateformat.parse(betStartTime).getTime();
				long t2 = dateformat.parse(betEndTime).getTime();
				int t = (int) (t2 - t1) / 1000;
				countDown = (int) Math.round(t / 5d) * 5; // 以5秒钟为基准进行四舍五入，如14秒变成15秒，19秒变成20秒，26秒变成25秒
				logger.info(
						"下注倒计时=====>" + countDown + " 开始下注时间=====>" + betStartTime + " 结束下注时间======>" + betEndTime);
			} catch (ParseException e) {
				logger.error("", e);
			}
		}
	}

	public int controlNextMatch(String result) {
		designatedGame = DBHelper.getHelper().getDesignatedGame(currentGame, "WIN".equalsIgnoreCase(result) ? 1 : 2);
		CardCache.getCache().reset(designatedGame.getOriginalShoeId(), designatedGame.getGameId()+1);
		logger.info("The next match is changed[" + result + "]：\n currentGame==>" + currentGame + "\n designatedGame==>"
				+ designatedGame);
		return designatedGame == null ? -1 : designatedGame.getId();
	}

	/**
	 * @param args
	 */
	public static void main(String[] args) {

		int i=0;
		System.out.println(i++);
	}

}