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
import org.compiere.util.Trx;
import org.osgi.service.event.Event;

public class MatrixEventHandler extends AbstractEventHandler {

	private boolean bypass = false;
	
	@Override
	protected void initialize() {
		if(DB.isTableOrViewExists("LIT_M_Product_Category")) {
			registerTableEvent(IEventTopics.PO_AFTER_NEW, "M_InOutLine");
			registerTableEvent(IEventTopics.PO_AFTER_CHANGE, "M_InOutLine");
			registerTableEvent(IEventTopics.PO_BEFORE_CHANGE, "M_InOutLine");
			registerTableEvent(IEventTopics.PO_BEFORE_NEW, "M_InOutLine");
		}
	}

	@Override
	protected void doHandleEvent(Event event) {
		PO po = getPO(event);
		if(po.get_TableName().equals("M_InOutLine") && po.columnExists("LIT_M_Product_Category_ID") && po.columnExists("LineNetAmt") && 
				(po.is_ValueChanged("QtyEntered") || bypass || (!bypass && po.columnExists("NewCostPrice") && po.get_Value("NewCostPrice")!=null && ((BigDecimal)po.get_Value("NewCostPrice")).compareTo(BigDecimal.ZERO)>0))) {
			if(event.getTopic().equals(IEventTopics.PO_AFTER_NEW) || event.getTopic().equals(IEventTopics.PO_AFTER_CHANGE)){

				int adClientID = po.getAD_Client_ID();
				Timestamp shipDate = (Timestamp)po.get_Value("ShipDate");
				//Per problemi di RELEASE SAVEPOINT, calcolo manuale dell'ultimo dato inserito
				BigDecimal sumLineNetAmt = new Query(Env.getCtx(), "M_InOutLine", "ShipDate=? AND LIT_M_Product_Category_ID=? AND M_InOutLine_ID!=? AND M_InOut_ID =?", null)
						.setClient_ID()
						.setOnlyActiveRecords(true)
						.setParameters(shipDate, po.get_ValueAsInt("LIT_M_Product_Category_ID"), po.get_ValueAsInt("M_InOutLine_ID"),po.get_ValueAsInt("M_InOut_ID"))
						.sum("LineNetAmt");
				
				  BigDecimal addAmt = BigDecimal.ZERO;
				  if(((BigDecimal)po.get_Value("QtyEntered")).compareTo(BigDecimal.ZERO)>0)
					  addAmt = (BigDecimal)po.get_Value("LineNetAmt"); 
				  if(addAmt==null) 
					  return;
				  sumLineNetAmt = sumLineNetAmt.add(addAmt);
//				  if(sumLineNetAmt.compareTo(BigDecimal.ZERO)<=0) 
//					  return;
				 
				////////
				
				String sqlUpdate = "UPDATE M_InOutLine SET QtyEntered=? WHERE AD_Client_ID=? AND ShipDate=? AND M_InOut_ID=? "
						+ "AND LIT_M_Product_Category_ID IN (SELECT M_Product_Category_Parent_ID FROM LIT_M_Product_Category WHERE AD_Client_ID=? AND LIT_M_Product_Category_ID=?)";
				DB.executeUpdate(sqlUpdate, new Object[] {sumLineNetAmt, adClientID, shipDate, po.get_ValueAsInt("M_InOut_ID"), adClientID, po.get_ValueAsInt("LIT_M_Product_Category_ID")}, false, null);
				bypass = false;
				
			}
			else if(event.getTopic().equals(IEventTopics.PO_BEFORE_NEW) || event.getTopic().equals(IEventTopics.PO_BEFORE_CHANGE)){
				if(((BigDecimal)po.get_Value("QtyEntered")).compareTo(BigDecimal.ZERO)<=0)
					po.set_ValueOfColumn("LineNetAmt", BigDecimal.ZERO);
				bypass = true;
			}
		}
	}

}
