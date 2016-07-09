package org.tom.vd.cache;

import java.util.List;
import java.util.Map;
import java.util.concurrent.LinkedBlockingDeque;

import org.apache.log4j.Logger;
import org.tom.vd.VirtualDealer;
import org.tom.vd.bean.GameRoundInfo;
import org.tom.vd.config.Config;
import org.tom.vd.db.DBHelper;

public class CardCache {

    private static final Logger logger = Logger.getLogger(CardCache.class);

    private LinkedBlockingDeque<GameRoundInfo> queue = new LinkedBlockingDeque<GameRoundInfo>();

    private Boolean isFill = false;

    private int startShoeId = -1, startGameId = -1;

    private static CardCache instance = new CardCache();

    private GameRoundInfo g = null;

    private Config cfg = Config.getCfg();

    private CardCache() {
        String strStartShoeId=cfg.getString("game.startShoeId","-1");
        String strStartGameId=cfg.getString("game.startGameId","-1");
        logger.info("strStartShoeId=============>"+strStartShoeId+"====startGameId======>"+startGameId);
        startShoeId=Integer.parseInt(strStartShoeId);
        startGameId=Integer.parseInt(strStartGameId);
    }

    public static CardCache getCache() {
        return instance;
    }

    /**
     * @return
     */
    public GameRoundInfo poll() {
        if (queue.isEmpty() && !isFill) {
            new FillQueueThread().start();
        }

        try {
            g = queue.take();
        } catch (InterruptedException e) {
        }
        return g;
    }

    class FillQueueThread extends Thread {

        @Override
        public void run() {
            logger.info("Begin fill queue from db  cache size ==>"
                    + queue.size());
            synchronized (isFill) {
                isFill = true;
            }


            DBHelper helper = DBHelper.getHelper();

            List<GameRoundInfo> list = null;

            int vShoeId = helper.getShoeid(startShoeId == -1);

            if (startShoeId != -1) { //从指定的牌局开始播放
                list = helper.getDesignatedGames(startShoeId, startGameId);
                startShoeId = startGameId = -1;
            } else {
                int originalShoeId = 1;
                if (g != null) {
                    originalShoeId = helper.getNextShoeid(g);
                } else {
                    originalShoeId = helper.getOriginalShoeIdForStart();
                }
                list = helper.getGameRoundInfo(originalShoeId);
                helper.updatePlayTime(originalShoeId);
            }

            for (GameRoundInfo game : list) {
                game.setShoeid(vShoeId);
                logger.debug("Get game==>"
                        + game);
                queue.addLast(game);
            }
            synchronized (isFill) {
                isFill = false;
            }
            logger.info("end fill queue from db  cache size ==>"
                    + queue.size());
        }

    }

    public static void main(String[] args) throws InterruptedException {
        CardCache.getCache().poll();
    }
}
