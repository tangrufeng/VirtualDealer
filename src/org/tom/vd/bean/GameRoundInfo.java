package org.tom.vd.bean;

/**
 * <p>Title: GameRoundInfo.java</p>
 * <p>Description: </p>
 * <p>Copyright: Copyright (c) 2014</p>
 * <p>Company: jc-yt.com</p>
 * @author tang
 * @date 2015-12-20 下午4:22:34
 * @version 1.0
 *
 */
public class GameRoundInfo implements Cloneable{

	private int id;
	
	private int result;
	
	private String createTime;
	
	private String gameType;
	
	private String videoPath;
	
	private String cardInfo;
	
	private int dealer;
	
	private int groupId;
	
	private String lastPlayTime;
	
	private int shoeid;
	
	private int originalShoeId;

	private int roomId;
	
	private int gameId;
	
	private String cardBGColor;
	
	private String chargeShoe;
	

	public String getChargeShoe() {
		return chargeShoe;
	}

	public void setChargeShoe(String chargeShoe) {
		this.chargeShoe = chargeShoe;
	}

	public int getOriginalShoeId() {
		return originalShoeId;
	}

	public void setOriginalShoeId(int originalShoeId) {
		this.originalShoeId = originalShoeId;
	}

	public String getCardBGColor() {
		return cardBGColor;
	}

	public void setCardBGColor(String cardBGColor) {
		this.cardBGColor = cardBGColor;
	}

	public int getGameId() {
		return gameId;
	}

	public void setGameId(int gameId) {
		this.gameId = gameId;
	}

	public int getRoomId() {
		return roomId;
	}

	public void setRoomId(int roomId) {
		this.roomId = roomId;
	}

	public int getShoeid() {
		return shoeid;
	}

	public void setShoeid(int shoeid) {
		this.shoeid = shoeid;
	}

	public int getId() {
		return id;
	}

	public void setId(int id) {
		this.id = id;
	}

	public int getResult() {
		return result;
	}

	public void setResult(int result) {
		this.result = result;
	}

	public String getCreateTime() {
		return createTime;
	}

	public void setCreateTime(String createTime) {
		this.createTime = createTime;
	}

	public String getGameType() {
		return gameType;
	}

	public void setGameType(String gameType) {
		this.gameType = gameType;
	}

	public String getVideoPath() {
		return videoPath;
	}

	public void setVideoPath(String videoPath) {
		this.videoPath = videoPath;
	}

	public String getCardInfo() {
		return cardInfo;
	}

	public void setCardInfo(String cardInfo) {
		this.cardInfo = cardInfo;
	}

	public int getDealer() {
		return dealer;
	}

	public void setDealer(int dealer) {
		this.dealer = dealer;
	}

	public int getGroupId() {
		return groupId;
	}

	public void setGroupId(int groupId) {
		this.groupId = groupId;
	}

	public String getLastPlayTime() {
		return lastPlayTime;
	}

	public void setLastPlayTime(String lastPlayTime) {
		this.lastPlayTime = lastPlayTime;
	}

	

	@Override
	public String toString() {
		return "GameRoundInfo [id=" + id + ", result=" + result
				+ ", createTime=" + createTime + ", gameType=" + gameType
				+ ", videoPath=" + videoPath + ", cardInfo=" + cardInfo
				+ ", dealer=" + dealer + ", groupId=" + groupId
				+ ", lastPlayTime=" + lastPlayTime + ", shoeid=" + shoeid
				+ ", originalShoeId=" + originalShoeId + ", roomId=" + roomId
				+ ", gameId=" + gameId + ", cardBGColor=" + cardBGColor
				+ ", chargeShoe=" + chargeShoe + "]";
	}

	@Override
	protected Object clone() {
		
		GameRoundInfo game=null;
		try {  
			game = (GameRoundInfo) super.clone();  
        } catch (CloneNotSupportedException e) {  
            e.printStackTrace();  
        }  
        return game; 
	}

	
	
	
	
	

}
