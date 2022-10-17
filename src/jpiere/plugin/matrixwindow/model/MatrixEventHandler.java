package jpiere.plugin.matrixwindow.model;

import java.math.BigDecimal;
import java.sql.SQLException;
import java.sql.Timestamp;

import org.adempiere.base.event.AbstractEventHandler;
import org.adempiere.base.event.IEventTopics;
import org.compiere.model.PO;
import org.compiere.model.Query;
import org.compiere.util.DB;
import org.compiere.util.Env;
import org.osgi.service.event.Event;

public class MatrixEventHandler extends AbstractEventHandler {

	@Override
	protected void initialize() {
		if(DB.isTableOrViewExists("LIT_M_Product_Category")) {
			registerTableEvent(IEventTopics.PO_AFTER_NEW, "M_InOutLine");
			registerTableEvent(IEventTopics.PO_AFTER_CHANGE, "M_InOutLine");
		}
	}

	@Override
	protected void doHandleEvent(Event event) {
		if(event.getTopic().equals(IEventTopics.PO_AFTER_NEW) || event.getTopic().equals(IEventTopics.PO_AFTER_CHANGE)){
			PO po = getPO(event);
			
			if(po.get_TableName().equals("M_InOutLine") && po.columnExists("LIT_M_Product_Category_ID") && po.columnExists("LineNetAmt")) {
				try {
					DB.commit(false, po.get_TrxName());
				} catch (IllegalStateException e) {
					e.printStackTrace();
				} catch (SQLException e) {
					e.printStackTrace();
				}
				Timestamp shipDate = (Timestamp)po.get_Value("ShipDate");
				BigDecimal sumLineNetAmt = new Query(Env.getCtx(), "M_InOutLine", "ShipDate=? AND LIT_M_Product_Category_ID=?", null)
						.setClient_ID()
						.setOnlyActiveRecords(true)
						.setParameters(shipDate, po.get_ValueAsInt("LIT_M_Product_Category_ID"))
						.sum("LineNetAmt");
				int adClientID = po.getAD_Client_ID();
				String sqlUpdate = "UPDATE M_InOutLine SET QtyEntered=? WHERE AD_Client_ID=? AND ShipDate=? "
						+ "AND LIT_M_Product_Category_ID IN (SELECT M_Product_Category_Parent_ID FROM LIT_M_Product_Category WHERE AD_Client_ID=? AND LIT_M_Product_Category_ID=?)";
				DB.executeUpdate(sqlUpdate, new Object[] {sumLineNetAmt, adClientID, shipDate, adClientID, po.get_ValueAsInt("LIT_M_Product_Category_ID")}, false, null);
				
			}
		}
	}

}
