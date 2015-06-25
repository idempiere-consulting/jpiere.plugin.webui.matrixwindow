/******************************************************************************
 * Product: iDempiere ERP & CRM Smart Business Solution                       *
 * Copyright (C) 1999-2012 ComPiere, Inc. All Rights Reserved.                *
 * This program is free software, you can redistribute it and/or modify it    *
 * under the terms version 2 of the GNU General Public License as published   *
 * by the Free Software Foundation. This program is distributed in the hope   *
 * that it will be useful, but WITHOUT ANY WARRANTY, without even the implied *
 * warranty of MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.           *
 * See the GNU General Public License for more details.                       *
 * You should have received a copy of the GNU General Public License along    *
 * with this program, if not, write to the Free Software Foundation, Inc.,    *
 * 59 Temple Place, Suite 330, Boston, MA 02111-1307 USA.                     *
 * For the text or an alternative of this public license, you may reach us    *
 * ComPiere, Inc., 2620 Augustine Dr. #245, Santa Clara, CA 95054, USA        *
 * or via info@compiere.org or http://www.compiere.org/license.html           *
 *****************************************************************************/
/** Generated Model - DO NOT CHANGE */
package jpiere.plugin.matrixwindow.model;

import java.sql.ResultSet;
import java.util.Properties;
import org.compiere.model.*;

/** Generated Model for JP_MatrixWindow
 *  @author iDempiere (generated) 
 *  @version Release 2.1 - $Id$ */
public class X_JP_MatrixWindow extends PO implements I_JP_MatrixWindow, I_Persistent 
{

	/**
	 *
	 */
	private static final long serialVersionUID = 20150625L;

    /** Standard Constructor */
    public X_JP_MatrixWindow (Properties ctx, int JP_MatrixWindow_ID, String trxName)
    {
      super (ctx, JP_MatrixWindow_ID, trxName);
      /** if (JP_MatrixWindow_ID == 0)
        {
			setAD_Tab_ID (0);
			setAD_Window_ID (0);
			setJP_MatrixColumnKey_ID (0);
			setJP_MatrixRowKey_ID (0);
			setJP_MatrixWindow_ID (0);
			setName (null);
			setParent_Column_ID (0);
			setValue (null);
        } */
    }

    /** Load Constructor */
    public X_JP_MatrixWindow (Properties ctx, ResultSet rs, String trxName)
    {
      super (ctx, rs, trxName);
    }

    /** AccessLevel
      * @return 4 - System 
      */
    protected int get_AccessLevel()
    {
      return accessLevel.intValue();
    }

    /** Load Meta Data */
    protected POInfo initPO (Properties ctx)
    {
      POInfo poi = POInfo.getPOInfo (ctx, Table_ID, get_TrxName());
      return poi;
    }

    public String toString()
    {
      StringBuffer sb = new StringBuffer ("X_JP_MatrixWindow[")
        .append(get_ID()).append("]");
      return sb.toString();
    }

	public org.compiere.model.I_AD_Tab getAD_Tab() throws RuntimeException
    {
		return (org.compiere.model.I_AD_Tab)MTable.get(getCtx(), org.compiere.model.I_AD_Tab.Table_Name)
			.getPO(getAD_Tab_ID(), get_TrxName());	}

	/** Set Tab.
		@param AD_Tab_ID 
		Tab within a Window
	  */
	public void setAD_Tab_ID (int AD_Tab_ID)
	{
		if (AD_Tab_ID < 1) 
			set_Value (COLUMNNAME_AD_Tab_ID, null);
		else 
			set_Value (COLUMNNAME_AD_Tab_ID, Integer.valueOf(AD_Tab_ID));
	}

	/** Get Tab.
		@return Tab within a Window
	  */
	public int getAD_Tab_ID () 
	{
		Integer ii = (Integer)get_Value(COLUMNNAME_AD_Tab_ID);
		if (ii == null)
			 return 0;
		return ii.intValue();
	}

	public org.compiere.model.I_AD_Window getAD_Window() throws RuntimeException
    {
		return (org.compiere.model.I_AD_Window)MTable.get(getCtx(), org.compiere.model.I_AD_Window.Table_Name)
			.getPO(getAD_Window_ID(), get_TrxName());	}

	/** Set Window.
		@param AD_Window_ID 
		Data entry or display window
	  */
	public void setAD_Window_ID (int AD_Window_ID)
	{
		if (AD_Window_ID < 1) 
			set_Value (COLUMNNAME_AD_Window_ID, null);
		else 
			set_Value (COLUMNNAME_AD_Window_ID, Integer.valueOf(AD_Window_ID));
	}

	/** Get Window.
		@return Data entry or display window
	  */
	public int getAD_Window_ID () 
	{
		Integer ii = (Integer)get_Value(COLUMNNAME_AD_Window_ID);
		if (ii == null)
			 return 0;
		return ii.intValue();
	}

	/** Set Description.
		@param Description 
		Optional short description of the record
	  */
	public void setDescription (String Description)
	{
		set_Value (COLUMNNAME_Description, Description);
	}

	/** Get Description.
		@return Optional short description of the record
	  */
	public String getDescription () 
	{
		return (String)get_Value(COLUMNNAME_Description);
	}

	public org.compiere.model.I_AD_Field getJP_MatrixColumnKey() throws RuntimeException
    {
		return (org.compiere.model.I_AD_Field)MTable.get(getCtx(), org.compiere.model.I_AD_Field.Table_Name)
			.getPO(getJP_MatrixColumnKey_ID(), get_TrxName());	}

	/** Set JP_MatrixColumnKey_ID.
		@param JP_MatrixColumnKey_ID JP_MatrixColumnKey_ID	  */
	public void setJP_MatrixColumnKey_ID (int JP_MatrixColumnKey_ID)
	{
		if (JP_MatrixColumnKey_ID < 1) 
			set_Value (COLUMNNAME_JP_MatrixColumnKey_ID, null);
		else 
			set_Value (COLUMNNAME_JP_MatrixColumnKey_ID, Integer.valueOf(JP_MatrixColumnKey_ID));
	}

	/** Get JP_MatrixColumnKey_ID.
		@return JP_MatrixColumnKey_ID	  */
	public int getJP_MatrixColumnKey_ID () 
	{
		Integer ii = (Integer)get_Value(COLUMNNAME_JP_MatrixColumnKey_ID);
		if (ii == null)
			 return 0;
		return ii.intValue();
	}

	public org.compiere.model.I_AD_Field getJP_MatrixRowKey() throws RuntimeException
    {
		return (org.compiere.model.I_AD_Field)MTable.get(getCtx(), org.compiere.model.I_AD_Field.Table_Name)
			.getPO(getJP_MatrixRowKey_ID(), get_TrxName());	}

	/** Set JP_MatrixRowKey_ID.
		@param JP_MatrixRowKey_ID JP_MatrixRowKey_ID	  */
	public void setJP_MatrixRowKey_ID (int JP_MatrixRowKey_ID)
	{
		if (JP_MatrixRowKey_ID < 1) 
			set_Value (COLUMNNAME_JP_MatrixRowKey_ID, null);
		else 
			set_Value (COLUMNNAME_JP_MatrixRowKey_ID, Integer.valueOf(JP_MatrixRowKey_ID));
	}

	/** Get JP_MatrixRowKey_ID.
		@return JP_MatrixRowKey_ID	  */
	public int getJP_MatrixRowKey_ID () 
	{
		Integer ii = (Integer)get_Value(COLUMNNAME_JP_MatrixRowKey_ID);
		if (ii == null)
			 return 0;
		return ii.intValue();
	}

	/** Set JP_MatrixWindow.
		@param JP_MatrixWindow_ID JP_MatrixWindow	  */
	public void setJP_MatrixWindow_ID (int JP_MatrixWindow_ID)
	{
		if (JP_MatrixWindow_ID < 1) 
			set_ValueNoCheck (COLUMNNAME_JP_MatrixWindow_ID, null);
		else 
			set_ValueNoCheck (COLUMNNAME_JP_MatrixWindow_ID, Integer.valueOf(JP_MatrixWindow_ID));
	}

	/** Get JP_MatrixWindow.
		@return JP_MatrixWindow	  */
	public int getJP_MatrixWindow_ID () 
	{
		Integer ii = (Integer)get_Value(COLUMNNAME_JP_MatrixWindow_ID);
		if (ii == null)
			 return 0;
		return ii.intValue();
	}

	/** Set JP_MatrixWindow_UU.
		@param JP_MatrixWindow_UU JP_MatrixWindow_UU	  */
	public void setJP_MatrixWindow_UU (String JP_MatrixWindow_UU)
	{
		set_ValueNoCheck (COLUMNNAME_JP_MatrixWindow_UU, JP_MatrixWindow_UU);
	}

	/** Get JP_MatrixWindow_UU.
		@return JP_MatrixWindow_UU	  */
	public String getJP_MatrixWindow_UU () 
	{
		return (String)get_Value(COLUMNNAME_JP_MatrixWindow_UU);
	}

	/** Set Name.
		@param Name 
		Alphanumeric identifier of the entity
	  */
	public void setName (String Name)
	{
		set_Value (COLUMNNAME_Name, Name);
	}

	/** Get Name.
		@return Alphanumeric identifier of the entity
	  */
	public String getName () 
	{
		return (String)get_Value(COLUMNNAME_Name);
	}

	public org.compiere.model.I_AD_Column getParent_Column() throws RuntimeException
    {
		return (org.compiere.model.I_AD_Column)MTable.get(getCtx(), org.compiere.model.I_AD_Column.Table_Name)
			.getPO(getParent_Column_ID(), get_TrxName());	}

	/** Set Parent Column.
		@param Parent_Column_ID 
		The link column on the parent tab.
	  */
	public void setParent_Column_ID (int Parent_Column_ID)
	{
		if (Parent_Column_ID < 1) 
			set_Value (COLUMNNAME_Parent_Column_ID, null);
		else 
			set_Value (COLUMNNAME_Parent_Column_ID, Integer.valueOf(Parent_Column_ID));
	}

	/** Get Parent Column.
		@return The link column on the parent tab.
	  */
	public int getParent_Column_ID () 
	{
		Integer ii = (Integer)get_Value(COLUMNNAME_Parent_Column_ID);
		if (ii == null)
			 return 0;
		return ii.intValue();
	}

	/** Set Search Key.
		@param Value 
		Search key for the record in the format required - must be unique
	  */
	public void setValue (String Value)
	{
		set_Value (COLUMNNAME_Value, Value);
	}

	/** Get Search Key.
		@return Search key for the record in the format required - must be unique
	  */
	public String getValue () 
	{
		return (String)get_Value(COLUMNNAME_Value);
	}
}