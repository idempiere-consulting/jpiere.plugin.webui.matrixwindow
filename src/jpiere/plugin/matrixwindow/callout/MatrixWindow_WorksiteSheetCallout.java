/******************************************************************************
 * Product: JPiere                                                            *
 * Copyright (C) Hideaki Hagiwara (h.hagiwara@oss-erp.co.jp)                  *
 *                                                                            *
 * This program is free software, you can redistribute it and/or modify it    *
 * under the terms version 2 of the GNU General Public License as published   *
 * by the Free Software Foundation. This program is distributed in the hope   *
 * that it will be useful, but WITHOUT ANY WARRANTY.                          *
 * See the GNU General Public License for more details.                       *
 *                                                                            *
 * JPiere is maintained by OSS ERP Solutions Co., Ltd.                        *
 * (http://www.oss-erp.co.jp)                                                 *
 *****************************************************************************/
package jpiere.plugin.matrixwindow.callout;

import java.math.BigDecimal;
import java.math.RoundingMode;

import org.compiere.model.GridField;
import org.compiere.model.MColumn;
import org.compiere.model.MInOut;
import org.compiere.model.MInOutLine;
import org.compiere.model.MPriceList;
import org.compiere.model.MPriceListVersion;
import org.compiere.model.MProduct;
import org.compiere.model.MProductPrice;
import org.compiere.model.PO;
import org.compiere.util.Env;

import jpiere.plugin.matrixwindow.base.IMatrixWindowCallout;
import jpiere.plugin.matrixwindow.form.JPMatrixDataBinder;

public class MatrixWindow_WorksiteSheetCallout implements IMatrixWindowCallout {

	@Override
	public String start(JPMatrixDataBinder dataBinder, PO po, int x, int y , Object newValue, Object oldValue)
	{

		GridField gridField = dataBinder.getColumnGridFieldMap().get(x);
		//int tabNo = gridField.getGridTab().getTabNo();


		int PriceList_ID = 0;
		int StdPrecision = 0;

		BigDecimal qty = (BigDecimal)newValue;
		if(qty==null || qty.compareTo(BigDecimal.ZERO)<=0)
			return "";


		MInOutLine outLine = (MInOutLine)po;
		MInOut inout = new MInOut (po.getCtx(), outLine.getM_InOut_ID() ,null);
		//SoTrx=inout.get_Value("isSoTrx");

		//if (SoTrx.equals(true) ) {

		if( outLine.getM_Product_ID() > 0 ) {
			MProduct p = new MProduct(po.getCtx(), outLine.getM_Product_ID() ,null);

			//se il pricelist della riga è valorizzato prendo quello altrimenti quello della testata
			if (outLine.get_ValueAsInt("BasePriceList_ID") > 0)
				PriceList_ID = outLine.get_ValueAsInt("BasePriceList_ID"); //m_inoutline
			else 
				PriceList_ID =inout.get_ValueAsInt("M_PriceList_ID"); //m_inout

			StdPrecision = MPriceList.getPricePrecision(po.getCtx(), PriceList_ID);
			MPriceList priceList = new MPriceList(po.getCtx(), PriceList_ID, null);
			if(priceList != null) {
				MPriceListVersion priceListVersion = priceList.getPriceListVersion(inout.getMovementDate());
				MProductPrice prodPrice = null; 
				if (priceListVersion!=null)
					prodPrice = MProductPrice.get(po.getCtx(), priceListVersion.getM_PriceList_Version_ID(), outLine.getM_Product_ID() , null);

				if (priceListVersion!=null) {
					//Si scatena la CALLOUT se sono sulla Column "Prodotto" o sulla PriceList
					if( (gridField.getColumnName().equals("M_Product_ID")) || (gridField.getColumnName().equals("BasePriceList_ID")) ) {
						if(prodPrice != null) {
							BigDecimal PriceStd = prodPrice.getPriceStd();
							BigDecimal PriceList = prodPrice.getPriceList();
							if (PriceStd.scale() > StdPrecision)
								PriceStd = PriceStd.setScale(StdPrecision, RoundingMode.HALF_UP);
							outLine.set_ValueOfColumn("PriceEntered", PriceStd);
							outLine.set_ValueOfColumn("PriceList", PriceList);
						} else {
							outLine.set_ValueOfColumn("PriceEntered",  Env.ZERO);
							outLine.set_ValueOfColumn("PriceList",  Env.ZERO);
						}
					}  
				}

				//Si scatena la CALLOUT del Discount se sono sul Prodotto, sul QtyEntered  o PriceList
				if((gridField.getColumnName().equals("M_Product_ID")) || (gridField.getColumnName().equals("PriceEntered")) || (gridField.getColumnName().equals("PriceList")) ) {
					if ((outLine.get_Value("PriceList") != null) && (outLine.get_Value("PriceEntered") != null) ) {
						BigDecimal Discount = null;
						if (((BigDecimal)outLine.get_Value("PriceList")).compareTo(Env.ZERO) == 0)
							Discount = Env.ZERO;
						else
							Discount = BigDecimal.valueOf((((BigDecimal)outLine.get_Value("PriceList")).doubleValue() - ((BigDecimal)outLine.get_Value("PriceEntered")).doubleValue()) /  ((BigDecimal)outLine.get_Value("PriceList")).doubleValue() * 100.0);
						if (Discount.scale() > 2)
							Discount = Discount.setScale(2, RoundingMode.HALF_UP);
						outLine.set_ValueOfColumn("Discount", Discount);
					}
				}


				//Si scatena la CALLOUT se sono sulla Column "Discount"
				if(gridField.getColumnName().equals("Discount")) {
					if ((outLine.get_Value("PriceList") != null) && (outLine.get_Value("PriceEntered") != null) && (outLine.get_Value("Discount") != null) ) {
						BigDecimal PriceStandard = null;
						if (((BigDecimal)outLine.get_Value("PriceList")).doubleValue() != 0 )
							PriceStandard = BigDecimal.valueOf((100.0 - ((BigDecimal)outLine.get_Value("Discount")).doubleValue()) / 100.0 * ((BigDecimal)outLine.get_Value("PriceList")).doubleValue());
						if (PriceStandard.scale() > StdPrecision)
							PriceStandard = PriceStandard.setScale(StdPrecision, RoundingMode.HALF_UP);
						outLine.set_ValueOfColumn("PriceEntered", PriceStandard);
					}
				}


				//calcolo il LineNetAmt
				if (outLine.get_Value("PriceEntered") != null) {
					//iDempiereConsulting __30/10/2023 --- Calcolo di eventuale costo gasolio (CLIENTE: Limestone)
					//BigDecimal LineNetAmt = ((BigDecimal)outLine.get_Value("QtyEntered")).multiply((BigDecimal)outLine.get_Value("PriceEntered"));
					BigDecimal LineNetAmt = BigDecimal.ZERO;
					BigDecimal qtyEntered = (BigDecimal)outLine.get_Value("QtyEntered");
					BigDecimal priceEntered = (BigDecimal)outLine.get_Value("PriceEntered");
					if(outLine.columnExists("NewCostPrice") && outLine.get_Value("NewCostPrice")!=null && ((BigDecimal)outLine.get_Value("NewCostPrice")).compareTo(BigDecimal.ZERO)>0) {
						LineNetAmt = priceEntered.multiply(BigDecimal.ONE);
						if(qtyEntered.compareTo(BigDecimal.ONE)>0)
							LineNetAmt = LineNetAmt.add(qtyEntered.multiply((BigDecimal)outLine.get_Value("NewCostPrice")));
					}
					else
						LineNetAmt = qtyEntered.multiply(priceEntered);
					
					if (LineNetAmt.scale() > 2)
						LineNetAmt = LineNetAmt.setScale(2, RoundingMode.HALF_UP);
					outLine.set_ValueOfColumn("LineNetAmt", LineNetAmt);
				}  
				//}

//				//calcolo il LineNetAmtPlanned
//				if (outLine.get_Value("PriceEntered") != null) {
//					BigDecimal LineNetAmtPlanned = ((BigDecimal)outLine.get_Value("PlannedQty")).multiply((BigDecimal)outLine.get_Value("PriceEntered"));
//					if (LineNetAmtPlanned.scale() > 2)
//						LineNetAmtPlanned = LineNetAmtPlanned.setScale(2, RoundingMode.HALF_UP);
//					outLine.set_ValueOfColumn("LineNetAmtPlanned", LineNetAmtPlanned);
//				}  
			}
			
			outLine.saveEx();
		}
		return "";
	}

}
