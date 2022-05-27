package com.MeehanMetaSpace.swing;

import java.awt.Image;
import java.net.URL;
import java.util.HashMap;
import java.util.Map;

import javax.swing.Icon;
import javax.swing.ImageIcon;

import com.MeehanMetaSpace.Basics;


/**
 * Utility class for accessing icons stored in the MmsImages.jar 
 */
public final class MmsIcons {

	public static ImageIcon getNew() {
        return getImageIconPNG("new2");
    }
	
	public static ImageIcon getGoUp2() {
        return getImageIconPNG("go_up2");
    }
	
	public static ImageIcon getGoDown2() {
        return getImageIconPNG("go_down2");
    }
	
	public static ImageIcon getBullsEye() {
        return getImageIcon("bullseye");
    }
	
	public static ImageIcon getMaximize3() {
        return getImageIconPNG("maximize3");
    }
	
	public static ImageIcon getMinimize3() {
        return getImageIconPNG("minimize3");
    }
	
	public static ImageIcon getMaximize2() {
        return getImageIconPNG("maximize2");
    }
	
	public static ImageIcon getMinimize2() {
        return getImageIconPNG("minimize2");
    }
	
	public static ImageIcon getGoBack3() {
        return getImageIconPNG("go_back3");
    }
    
    public static ImageIcon getGoForward3() {
        return getImageIconPNG("go_forward3");
    }
    
	public static ImageIcon getGoBack() {
        return getImageIconPNG("go_back2");
    }
    
    public static ImageIcon getGoForward() {
        return getImageIconPNG("go_forward2");
    }
    
	public static ImageIcon getGoUp() {
        return getImageIconPNG("go_up");
    }
	
	public static ImageIcon getGoDown() {
        return getImageIconPNG("go_down");
    }
	
	public static ImageIcon getGoNext() {
        return getImageIconPNG("go_next");
    }
	
	public static ImageIcon getGoPrevious() {
        return getImageIconPNG("go_previous");
    }
	
	public static ImageIcon getRefresh() {
        return getImageIconPNG("refresh");
    }
	
	public static ImageIcon getSync() {
        return getImageIconPNG("sync");
    }
	
	public static ImageIcon getMaximize() {
        return getImageIconPNG("maximize");
    }
	
	public static ImageIcon getMinimize() {
        return getImageIconPNG("minimize");
    }
	
	public static ImageIcon getGenie() {
        return getImageIconPNG("Genie2");
    }
	
	public static ImageIcon getCytoGG() {
        return getImageIconPNG("CytoGG");
    }
	public static ImageIcon getLeonard() {
        return getImageIconPNG("Leonard2");
    }

	public static ImageIcon getLightningIcon() {
        return getImageIconPNG("lightning");
    }
    
    public static ImageIcon getSortAscending16Icon() {
        return getImageIcon("sortDown16");
    }

    public static ImageIcon getSortDescending16Icon() {
        return getImageIcon("sortUp16");
    }

    public static ImageIcon getSortCustomDown16Icon() {
        return getImageIcon("sortCustomDown16");
    }

    public static ImageIcon getTableRefreshIcon() {
        return getImageIcon("table_refresh");
    }


    public static ImageIcon getSumIcon() {
        return getImageIcon("sum");
    }

    public static ImageIcon getTableSaveIcon() {
        return getImageIcon("table_save");
    }

    public static ImageIcon getRowsIcon() {
        return getImageIcon("rows");
    }
    
    public static ImageIcon getColumnsIcon() {
        return getImageIcon("columns");
    }

    public static ImageIcon getTableRelationshipIcon() {
        return getImageIconPNG("table_relationship");
    }
    public static ImageIcon getTableAddIcon() {
        return getImageIconPNG("table_add");
    }
    public static ImageIcon getTagGreenIcon() {
        return getImageIconPNG("tag_green");
    }
    public static ImageIcon getTagBlueIcon() {
        return getImageIconPNG("tag_blue");
    }
    
    public static ImageIcon getSplashIcon() {
        return getImageIcon("Splash");
    }

    public static ImageIcon getTagBlueAddIcon() {
        return getImageIconPNG("tag_blue_add");
    }

    public static ImageIcon getTagBlueEditIcon() {
        return getImageIconPNG("tag_blue_edit");
    }

    public static ImageIcon getTagBlueDeleteIcon() {
        return getImageIconPNG("tag_blue_delete");
    }

    public static ImageIcon getEyeIcon() {
        return getImageIcon("eye");
    }
    
    public static ImageIcon getNewHelpIcon() {
        return getImageIcon("newHelp");
    }

    public static ImageIcon getCreditCardsIcon() {
        return getImageIcon("creditcards");
    }

    public static ImageIcon getTableEditIcon() {
        return getImageIcon("table_edit");
    }

    public static ImageIcon getTabAddIcon() {
        return getImageIcon("tab_add");
    }

    public static ImageIcon getTabDeleteIcon() {
        return getImageIcon("tab_delete");
    }
    

    public static ImageIcon getTableDeleteIcon() {
        return getImageIcon("table_delete");
    }

	public static ImageIcon getHeart16Icon() {
		return getImageIcon("heart");
	}

	public static ImageIcon getClock16Icon() {
		return getImageIcon("clock_play");
	}

	public static ImageIcon getChartOrg16Icon() {
		return getImageIcon("chart_organisation");
	}

	public static ImageIcon getColorSwatch16Icon() {
		return getImageIcon("color_swatch");
	}

	public static ImageIcon getWrench16Icon() {
        return getImageIcon("wrench_orange");
    }

    public static ImageIcon getSortAscendingIcon() {
        return getImageIcon("sortDown");
    }

    public static ImageIcon getSortDescendingIcon() {
        return getImageIcon("sortUp");
    }

    public static ImageIcon getSortCustomAscendingIcon() {
        return getImageIcon("sortCustomDown");
    }

    public static ImageIcon getSortCustomDescendingIcon() {
        return getImageIcon("sortCustomUp");
    }

    public static ImageIcon getSortCustomUp16Icon() {
        return getImageIcon("sortCustomUp16");
    }

    public static ImageIcon getImportIcon() {
        return getImageIcon("import");
    }

    public static ImageIcon getExportIcon() {
        return getImageIcon("export");
    }

    public static ImageIcon getHomeIcon() {
        return getImageIcon("home");
    }

    public static ImageIcon getBullsEyeIcon() {
        return getImageIcon("bullsEyeBig");
    }

    public static ImageIcon getLockIcon() {
        return getImageIcon("lock");
    }

    public static ImageIcon getUnlockIcon() {
        return getImageIcon("unlock");
    }

    public static ImageIcon getLockDisabledIcon() {
        return getImageIcon("lock_disabled");
    }

    public static ImageIcon getUnlockDisabledIcon() {
        return getImageIcon("unlock_disabled");
    }

    public static ImageIcon getEmailIcon() {
        return getImageIcon("email");
    }
    
    public static ImageIcon getRefresh24Icon() {
        return getImageIconPNG("refresh24");
    }

    public static ImageIcon getSearchIcon() {
        return getImageIcon("search");
    }

    public static ImageIcon getHelpIcon() {
        return getImageIcon("help");
    }
    public static ImageIcon getAcceptIcon() {
        return getImageIcon("accept");
    }

    public static ImageIcon getBookKeyIcon() {
        return getImageIcon("book_key");
    }
    public static ImageIcon getBookOpenIcon() {
        return getImageIcon("book_open");
    }

    public static ImageIcon getComputerGoIcon() {
        return getImageIcon("computer_go");
    }
    public static ImageIcon getComputerEditIcon() {
        return getImageIcon("computer_edit");
    }

    public static ImageIcon getPageFindIcon() {
        return getImageIcon("page_find");
    }

    public static ImageIcon getBugIcon() {
        return getImageIcon("bug");
    }

    public static ImageIcon getWeatherCloudsIcon() {
        return getImageIcon("weather_clouds");
    }
    
    public static ImageIcon getWeatherCloudyIcon() {
        return getImageIcon("weather_cloudy");
    }

    public static ImageIcon getWeatherSunIcon() {
        return getImageIcon("weather_sun");
    }

    public static ImageIcon getRainbowIcon() {
        return getImageIcon("rainbow");
    }

    public static ImageIcon getRainbowGrayIcon() {
        return getImageIcon("rainbowGray");
    }

    public static ImageIcon getTableMultiple() {
        return getImageIcon("table_multiple");
    }

    public static ImageIcon getChartBarIcon() {
        return getImageIcon("chart_bar");
    }
    public static ImageIcon getWrenchIcon() {
        return getImageIcon("wrench");
    }

    public static ImageIcon getCogAddIcon(){
    	return getImageIcon("cog_add");
    }
    public static ImageIcon getCogIcon(){
    	return getImageIcon("cog");
    }

    public static ImageIcon getGreatIcon() {
        return getImageIcon("great");
    }
    public static ImageIcon getLessIcon() {
        return getImageIcon("less");
    }
    
    public static ImageIcon getGoBackIcon() {
        return getImageIconPNG("go_back");
    }
    
    public static ImageIcon getGoForwardIcon() {
        return getImageIconPNG("go_forward");
    }
    
    public static ImageIcon getDownLittleIcon() {
        return getImageIcon("down");
    }
    public static ImageIcon getUpLittleIcon() {
        return getImageIcon("up");
    }
    
    public static ImageIcon getPinIcon() {
        return getImageIcon("pin");
    }

    public static ImageIcon getUnpinIcon() {
        return getImageIcon("unpin");
    }

    public static ImageIcon getEditIcon() {
        return getImageIcon("edit");
    }
    public static String NEW="new";
    public static ImageIcon getNewIcon() {
        return getImageIcon(NEW);
    }
    
    public static ImageIcon getNewTagIcon() {
        return getImageIcon("newTag");
    }

    public static ImageIcon getDeleteIcon() {
        return getImageIcon("delete16");
    }

    public static ImageIcon getRemoveIcon() {
        return getImageIcon("minus");
    }

    public static ImageIcon getAddIcon() {
        return getImageIcon("plus");
    }

    public static ImageIcon getRefreshIcon() {
        return getImageIcon("refresh16");
    }

    public static ImageIcon getMaximizeIcon() {
        return getImageIcon("maximize");
    }

    public static ImageIcon getRestoreIcon() {
        return getImageIcon("restore");
    }

    public static ImageIcon getFindIcon() {
        return getImageIcon("find16");
    }

    public static ImageIcon getFindAgainIcon() {
        return getImageIcon("findAgain16");
    }

    public static ImageIcon getMagnifyIcon() {
        return getImageIcon("magnify16");
    }

    public static ImageIcon getDragColumnsIcon() {
        return getImageIcon("drag_columns");
    }

    public static ImageIcon getSaveIcon() {
        return getImageIcon("save16");
    }

    public static ImageIcon getPreferencesIcon() {
        return getImageIcon("preferences16");
    }

    public static ImageIcon getOpenIcon() {
        return getImageIcon("open");
    }

    public static ImageIcon getCopyIcon() {
        return getImageIcon("copy");
    }

    public static ImageIcon getPasteIcon() {
        return getImageIcon("paste16");
    }

    public static ImageIcon getUpIcon() {
        return getImageIcon("upArrow");
    }

    public static ImageIcon getCanSelectIcon() {
        return getImageIcon("canSelect");
    }

    public static ImageIcon getCanNotSelectIcon() {
        return getImageIcon("canNotSelect");
    }

    public static ImageIcon getDownIcon() {
        return getImageIcon("downArrow");
    }

    public static ImageIcon getTopIcon() {
        return getImageIcon("moveTop");
    }

    public static ImageIcon getBottomIcon() {
        return getImageIcon("moveBottom");
    }

    public static ImageIcon getLeftIcon() {
        return getImageIcon("leftArrow");
    }

    public static ImageIcon getPrintIcon() {
        return getImageIcon("print16");
    }

    public static ImageIcon getPointIcon() {
        return getImageIcon("point");
    }
    
    public static ImageIcon getRightIcon() {
        return getImageIcon("rightArrow");
    }
    
    public static ImageIcon getArrowUpDoubleIcon() {
        return getImageIconPNG("arrow_up_double");
    }
    
    public static ImageIcon getArrowDownDoubleIcon() {
        return getImageIconPNG("arrow_down_double");
    }
    
    public static ImageIcon getNextIcon() {
        return getImageIconPNG("target");
    }

    public static ImageIcon getRedoIcon() {
        return getImageIcon("redo16");
    }

    public static ImageIcon getUndoIcon() {
        return getImageIcon("undo16");
    }

    public static ImageIcon getYesIcon() {
        return getImageIcon("yes");
    }

    public static ImageIcon getFontSelectedIcon() {
        return getImageIcon("fontSelected");
    }

    public static ImageIcon getBlankIcon() {
        return getImageIcon("blank");
    }

    public static ImageIcon getBlankNarrow13Icon() {
        return getImageIcon("blankNarrow13");
    }
    public static ImageIcon getBlank13x13Icon() {
        return getImageIcon("blank13x13");
    }

    public static ImageIcon getWideBlankIcon() {
        return getImageIcon("wideBlank");
    }

    public static ImageIcon getCancelIcon() {
        return getImageIcon("cancel");
    }

    public static ImageIcon getCloseIcon() {
        return getImageIcon("close");
    }

    public static ImageIcon getWorldSearchIcon() {
        return getImageIcon("worldSearch16");
    }


    public static ImageIcon getHandshakeIcon() {
        return getImageIcon("handshake16");
    }
    
    public static ImageIcon getSubjectIcon() {
        return getImageIcon("people");
    }
    
    public static ImageIcon getNoSubjectIcon() {
        return getImageIcon("noPeople");
    }
    
    public static ImageIcon getDragWebLinkHere() {
        return getImageIcon("dragWebLinksHere");
    }

    public static Image getPreferencesImage() {
        return getImageIcon("preferences16").getImage();
    }
    
    public static Image getNewImage() {
        return getImageIcon("new").getImage();
    }
    
    public final static String EDIT="edit";
    public static Image getEditImage() {
        return getImageIcon(EDIT).getImage();
    }
    
    public static Image getFindImage() {
        return getImageIcon("find24").getImage();
    }

    public static ImageIcon getGenieIcon() {
    	return getImageIcon("genieIcon");
    }
    
    public static ImageIcon getSpectrumIcon() {
    	return getImageIcon("spectrumbar");
    }

    public static Image getSpectraImage() {
    	return getImageIcon("spectra").getImage();
    }

    public static Image getLaserPointerImage() {
    	return getImageIcon("laserPointer").getImage();
    }
    public static ImageIcon getMenuItemArrowPageSoftIcon() {
        return getImageIconPNG("MenuItemArrowPageSoft");
    }

    public static ImageIcon getMenuItemArrowIcon() {
        return getImageIconPNG("MenuItemArrow");
    }
    public static ImageIcon getMenuItemArrowActivePageSoftIcon() {
        return getImageIconPNG("MenuItemArrowActivePageSoft");
    }
    
    public static ImageIcon getMenuItemArrowActiveIcon() {
        return getImageIconPNG("MenuItemArrowActive");
    }

    public static ImageIcon getMenuItemArrowSelectedPageSoftIcon() {
        return getImageIconPNG("MenuItemArrowSelectedPageSoft");
    }
    public static ImageIcon getMenuItemArrowSelectedIcon() {
        return getImageIconPNG("MenuItemArrowSelected");
    }
    public static ImageIcon getMenuItemArrowActiveSelectedPageSoftIcon() {
        return getImageIconPNG("MenuItemArrowActiveSelectedPageSoft");
    }
    
    public static ImageIcon getMenuItemArrowActiveSelectedIcon() {
        return getImageIconPNG("MenuItemArrowActiveSelected");
    }

    public static ImageIcon getMenuItemTiltedArrowIcon() {
        return getImageIconPNG("MenuItemTiltedArrow");
    }

    public static ImageIcon getCartIcon() {
        return getImageIconPNG("cart");
    }

    public static ImageIcon getYes16Icon() {
        return getImageIconPNG("yes16");
    }

    public static ImageIcon getFinishFlagIcon() {
        return getImageIconPNG("finishFlag");
    }

    public static ImageIcon getTickGreenIcon() {
        return getImageIconPNG("tick_green");
    }

    public static ImageIcon getStartFlagIcon() {
        return getImageIconPNG("flag_green");
    }

    public static ImageIcon getColorWheelIcon() {
        return getImageIconPNG("color_wheel");
    }

    public static ImageIcon getBlank16Icon() {
        return getImageIconPNG("blank16");
    }

    public static ImageIcon getWandIcon() {
        return getImageIconPNG("wand");
    }
    public static ImageIcon getGoogleIcon() {
        return getImageIconPNG("google");
    }

    public static ImageIcon getCartAddIcon() {
        return getImageIconPNG("cart_add");
    }
    public static ImageIcon getCartGoIcon() {
        return getImageIconPNG("cart_go");
    }
    public static ImageIcon getCartRemoveIcon() {
        return getImageIconPNG("cart_remove");
    }
    public static ImageIcon getCartPutIcon() {
        return getImageIconPNG("cart_put");
    }

    public static ImageIcon getBackIcon() {
        return getImageIconPNG("back");
    }

    public static ImageIcon getForwardIcon() {
        return getImageIconPNG("forward");
    }

    public static ImageIcon getReviewIconPNG() {
        return getImageIconPNG("review");
    }
    
    public static ImageIcon getNewIconPNG() {
        return getImageIconPNG("new_button");
    }
    
    
    public static ImageIcon getTableFreezeIcon() {
        return getImageIcon("application_side_contract");
    }

    public static ImageIcon getTableUnfreezeIcon() {
        return getImageIcon("application_side_expand");
    }

    public static ImageIcon getShowFilterRowsIcon() {
    	return getImageIcon("application_form_magnify");
    }
 
 
    public static ImageIcon getShowAllColumnsIcon() {
        return getImageIcon("application_form_add");
    }
    
    public static ImageIcon getSpacer() {
        return getImageIcon("spacer");
    }
 
    public static ImageIcon getHideColumnIcon() {
        return getImageIcon("application_form_delete");
    }
    
    public static ImageIcon getRestoreTableIcon() {
        return getImageIcon("application_view_columns");
    }

    public static ImageIcon getApplicationAddIcon() {
        return getImageIcon("application_add");
    }
    public static ImageIcon getApplicationDeleteIcon() {
        return getImageIcon("application_delete");
    }
    public static ImageIcon getPageWhiteCompressedIcon() {
        return getImageIcon("page_white_compressed");
    }

    public static ImageIcon getPreferredLockFailedIcon() {
        return getImageIcon("preferredLockFailed");
    }
    
    public static Icon getClueTubeIcon() {
		return getImageIconPNG("clueTube");
	}

    public static Icon getRedStarIcon() {
		return getImageIconPNG("redStar");
	}
    
    public static Icon getTickIcon() {
		return getImageIconPNG("tick_white2");
	}
    
    public static Icon getExclamationIcon() {
		return getImageIconPNG("star_red");
	}

    public static Icon getWarnIcon() {
		return getImageIconPNG("warn_icon");
	}
    public static Icon getNoRedIcon() {
		return getImageIconPNG("no");
	}
    
    public static Icon getTodoIcon() {
		return getImageIconJPG("todo");
	}
    
    public static Icon getMenuIcon() {
		return getImageIconJPG("menu");
	}
    
    public static Icon getSmallGenie() {
		return getImageIconPNG("smallGenie");
	}
    
	public static URL getURL(final String name) {
		final Class cl = MmsIcons.class;
		final String txt = Basics.concat("images/", name);
		URL u1 = cl.getResource(txt);
		return u1;
	}
	public static String getURLText(final String name) {
		final URL url=getURL(name);
		return url==null?"":url.toExternalForm();
	}
	
    private static ImageIcon getImageIcon(String name) {
        return SwingBasics.getImageIcon(MmsIcons.class, "images", name, ".gif");
    }

    private static ImageIcon getImageIconPNG(String name) {
        return SwingBasics.getImageIcon(MmsIcons.class, "images", name, ".png");
    }
    
    private static ImageIcon getImageIconJPG(String name) {
        return SwingBasics.getImageIcon(MmsIcons.class, "images", name, ".jpg");
    }

}
