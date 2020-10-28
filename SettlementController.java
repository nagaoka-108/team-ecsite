package jp.co.internous.cassiopeia.controller;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseBody;

import com.google.gson.Gson;

import jp.co.internous.cassiopeia.model.domain.MstDestination;
import jp.co.internous.cassiopeia.model.mapper.MstDestinationMapper;
import jp.co.internous.cassiopeia.model.mapper.TblCartMapper;
import jp.co.internous.cassiopeia.model.mapper.TblPurchaseHistoryMapper;
import jp.co.internous.cassiopeia.model.session.LoginSession;

@Controller
@RequestMapping("/cassiopeia/settlement")
public class SettlementController {
	
	@Autowired
	private LoginSession loginSession;
	
	@Autowired
	private TblCartMapper cartMapper;
	
	@Autowired
	private MstDestinationMapper destinationMapper;
	
	@Autowired
	private TblPurchaseHistoryMapper purchaseHistoryMapper;
	
	private Gson gson = new Gson();
	
	@RequestMapping("/")
	public String settlement(Model m) {
		//ユーザーに紐づき、有効な宛先情報のみ表示
		int userId = loginSession.getUserId();

		//DB(宛先情報マスターテーブル.mst_destination)から、ラジオ・宛先氏名・住所・電話番号等の全てのリストを取得
		List<MstDestination> destinations = destinationMapper.findByUserId(userId);
		m.addAttribute("destinations", destinations);
		m.addAttribute("loginSession", loginSession);
		
		return "settlement";
	}
	
	@SuppressWarnings("unchecked")
	@RequestMapping("/complete")
	@ResponseBody
	public boolean complete(@RequestBody String destinationId) {
		//画面から渡されたdestinationIdを取得。
		Map<String, String> map = gson.fromJson(destinationId, Map.class);
		String id = map.get("destinationId");
		
		//購入情報履歴テーブル(tbl_purchase_history)に決済情報を登録。
		int userId = loginSession.getUserId();
		Map<String, Object> parameter = new HashMap<>();
		parameter.put("destinationId", id);
		parameter.put("userId", userId);
		int insertCount = purchaseHistoryMapper.insert(parameter);
		
		//ユーザー情報に紐づいているカート情報テーブル.tbl_cartの情報を削除
		int deleteCount = 0;
		if (insertCount > 0) {
			deleteCount = cartMapper.deleteByUserId(userId);
		}
		return deleteCount == insertCount;
	}
}
