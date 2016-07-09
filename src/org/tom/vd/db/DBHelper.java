package org.tom.vd.db;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;

import org.apache.log4j.Logger;
import org.tom.vd.bean.GameRoundInfo;
import org.tom.vd.config.Config;

/*
 * @date 2016-1-10 下午2:43:33
 * @version 1.0
 * 
 */
public class DBHelper {

    private static Logger log = Logger.getLogger(DBHelper.class);

    private static DBHelper helper;
    public Connection conn = null;
    private Config cfg = Config.getCfg();

    private DBHelper() {
        try {
            Class.forName(cfg.getString("jdbc.driverClassName"));// 指定连接类型
            connDB();
        } catch (Exception e) {
            e.printStackTrace();
            log.error("连接数据库失败", e);
        }
    }

    private void connDB() {
        try {
            conn = DriverManager.getConnection(cfg.getString("jdbc.url"),
                    cfg.getString("jdbc.username"),
                    cfg.getString("jdbc.userpassword"));// 获取连接
            PreparedStatement ps = conn.prepareStatement("select 1 from dual");
            ResultSet rs = ps.executeQuery();
            while (rs.next()) {
                if (1 == rs.getInt(1)) {
                    log.error("数据库连接成功...");
                }
            }
        } catch (Exception e) {
            e.printStackTrace();
            log.error("连接数据库失败", e);
        }
    }

    public int getShoeid(boolean next) {
        int shoeId = 0;
        PreparedStatement ps = null;
        try {
            if (conn == null || conn.isClosed()) {
                connDB();
            }
            String sql = next ? cfg.getString("sql.getNextShoeId") : cfg.getString("sql.getCurrentShoeId");
            ps = conn.prepareStatement(sql);
            ps.setString(1, cfg.getString("game.shoeType"));
            ResultSet rs = ps.executeQuery();
            if (rs.next()) {
                shoeId = rs.getInt(1);
            }
        } catch (SQLException e) {
            e.printStackTrace();
            log.error("连接数据库失败", e);
        } finally {
            if (ps != null) {
                try {
                    ps.close();
                } catch (SQLException e) {
                }
            }
        }

        return shoeId;
    }


    public int getNextShoeid(GameRoundInfo currentGame) {
        PreparedStatement ps = null;
        try {
            if (conn == null || conn.isClosed()) {
                connDB();
            }
            ps = conn.prepareStatement(cfg.getString("sql.getOriginalShoeId"));
            String type = Config.getCfg().getString("game.type");
            String tableId = Config.getCfg().getString("game.tableid");
            ps.setInt(1, currentGame.getDealer());
            ps.setString(2, type);
            ps.setString(3, tableId);
            ps.setString(4, currentGame.getCardBGColor());

            ResultSet rs = ps.executeQuery();
            if (rs.next()) {
                return rs.getInt(1);
            }
        } catch (SQLException e) {
            e.printStackTrace();
            log.error("连接数据库失败", e);
        } finally {
            if (ps != null) {
                try {
                    ps.close();
                } catch (SQLException e) {
                }
            }
        }
        return 1;//如果没有找到，就返回第一靴
    }


    public int getOriginalShoeIdForStart() {
        PreparedStatement ps = null;
        try {
            if (conn == null || conn.isClosed()) {
                connDB();
            }
            ps = conn.prepareStatement(cfg.getString("sql.getOriginalShoeIdForStart"));
            String type = Config.getCfg().getString("game.type");
            String tableId = Config.getCfg().getString("game.tableid");
            ps.setString(1, type);
            ps.setString(2, tableId);
            ResultSet rs = ps.executeQuery();
            if (rs.next()) {
                return rs.getInt(1);
            }
        } catch (SQLException e) {
            e.printStackTrace();
            log.error("连接数据库失败", e);
        } finally {
            if (ps != null) {
                try {
                    ps.close();
                } catch (SQLException e) {
                }
            }
        }
        return 1;//如果没有找到，就返回第一靴
    }


    public GameRoundInfo getDesignatedGame(GameRoundInfo currentGame, int result) {
        PreparedStatement ps = null;
        try {
            if (conn == null || conn.isClosed()) {
                connDB();
            }
            int offSet = Integer.parseInt(Config.getCfg().getString(
                    "sql.gameOffset"));
            ps = conn.prepareStatement(cfg.getString("sql.getDesignatedGame"));
            ps.setInt(1, currentGame.getGameId() - offSet);
            ps.setInt(2, currentGame.getGameId() + offSet);
            ps.setString(3, currentGame.getGameType());
            ps.setString(4, currentGame.getCardBGColor());
            ps.setInt(5, currentGame.getRoomId());
            ps.setInt(6, currentGame.getDealer());
            ps.setInt(7, currentGame.getOriginalShoeId());

            ResultSet rs = ps.executeQuery();
            if (rs.next()) {
                GameRoundInfo g = fillGameRound(rs);
                return g;
            }
        } catch (SQLException e) {
            e.printStackTrace();
            log.error("连接数据库失败", e);
        } finally {
            if (ps != null) {
                try {
                    ps.close();
                } catch (SQLException e) {
                }
            }
        }
        return null;
    }

    /**
     * 获取某一靴的大于等于某局的牌局列表
     *
     * @param shoeId
     * @param gameId
     * @return
     */
    public List<GameRoundInfo> getDesignatedGames(int shoeId, int gameId) {
        List<GameRoundInfo> rst = new ArrayList<GameRoundInfo>();
        PreparedStatement ps = null;
        try {
            if (conn == null || conn.isClosed()) {
                connDB();
            }
            ps = conn.prepareStatement(cfg.getString("sql.getDesignatedGames"));
            String tableId = Config.getCfg().getString("game.tableid");
            ps.setInt(1, shoeId);
            ps.setInt(2, gameId);
            ps.setInt(3, Integer.parseInt(tableId));
            ResultSet rs = ps.executeQuery();
            while (rs.next()) {
                GameRoundInfo g = fillGameRound(rs);
                rst.add(g);
            }
        } catch (SQLException e) {
            log.error("连接数据库失败", e);
        } finally {
            if (ps != null) {
                try {
                    ps.close();
                } catch (SQLException e) {
                }
            }
        }
        return rst;
    }

    public List<GameRoundInfo> getGameRoundInfo(int shoeId) {
        List<GameRoundInfo> rst = new ArrayList<GameRoundInfo>();
        PreparedStatement ps = null;
        try {
            if (conn == null || conn.isClosed()) {
                connDB();
            }
            ps = conn.prepareStatement(cfg.getString("sql.getGameRoundInfo"));
            String type = Config.getCfg().getString("game.type");
            String tableId = Config.getCfg().getString("game.tableid");
            ps.setInt(1, shoeId);
            ps.setString(2, type);
            ps.setInt(3, Integer.parseInt(tableId));
            ResultSet rs = ps.executeQuery();
            while (rs.next()) {
                GameRoundInfo g = fillGameRound(rs);
                rst.add(g);
            }
        } catch (SQLException e) {
            log.error("连接数据库失败", e);
        } finally {
            if (ps != null) {
                try {
                    ps.close();
                } catch (SQLException e) {
                }
            }
        }
        return rst;
    }

    public void updatePlayTime(int shoeId) {
        PreparedStatement ps = null;
        try {
            if (conn == null || conn.isClosed()) {
                connDB();
            }
            ps = conn.prepareStatement(cfg.getString("sql.updatePlayerTime"));
            String type = Config.getCfg().getString("game.type");
            String tableId = Config.getCfg().getString("game.tableid");
            ps.setInt(1, shoeId);
            ps.setString(2, type);
            ps.setInt(3, Integer.parseInt(tableId));
            ps.executeUpdate();
        } catch (SQLException e) {
            log.error("连接数据库失败", e);
        } finally {
            if (ps != null) {
                try {
                    ps.close();
                } catch (SQLException e) {
                }
            }
        }

    }

    /**
     * @param rs
     * @return
     * @throws SQLException
     */
    private GameRoundInfo fillGameRound(ResultSet rs) throws SQLException {
        GameRoundInfo g = new GameRoundInfo();
        g.setId(rs.getInt(1));
        g.setResult(rs.getInt(2));
        g.setCreateTime(rs.getString(3));
        g.setGameType(rs.getString(4));
        g.setVideoPath(rs.getString(5));
        g.setCardInfo(rs.getString(6));
        g.setDealer(rs.getInt(7));
        g.setGroupId(rs.getInt(8));
        g.setOriginalShoeId(rs.getInt(10));
        g.setRoomId(rs.getInt(11));
        g.setGameId(rs.getInt(12));
        g.setCardBGColor(rs.getString(13));
        g.setChargeShoe(rs.getString(14));
        return g;
    }

    public static DBHelper getHelper() {
        if (helper == null) {
            helper = new DBHelper();
        }
        return helper;
    }

    public void close() {
        try {
            this.conn.close();
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }


}
