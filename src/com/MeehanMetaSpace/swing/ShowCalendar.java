package com.MeehanMetaSpace.swing;

import javax.swing.*;
import java.awt.*;
import java.awt.event.*;
import javax.swing.border.*;
import java.util.Calendar;
import com.MeehanMetaSpace.Basics;
/***************** ****************/
public class ShowCalendar
	extends JDialog{
  public java.util.Date rvalue;
  public int IYEAR;
  public int IMONTH;

  final JPanel calendarHeader=new javax.swing.JPanel();
  final JPanel calendarBody=new javax.swing.JPanel();
  final JPanel djp3=SwingBasics.getButtonPanel(3);
  final JButton today=SwingBasics.getButton(
	  "Today",
	  null,
	  't',
	  new ActionListener(){
	public void actionPerformed(ActionEvent e){
	  calendarHeader.removeAll();
	  calendarHeader.updateUI();
	  calendarBody.removeAll();
	  calendarBody.updateUI();
	  currentDay = (int) now.get(Calendar.DATE);
	  IYEAR=(int) now.get(Calendar.YEAR);
	  IMONTH=(int) now.get(Calendar.MONTH) + 1;
	  cal_main(Integer.toString(IYEAR), Integer.toString(IMONTH));
	}

  }

  ,
	  Basics.toHtmlUncentered("Today", "Position calandar at today's date"));
  JButton previousMonth=new JButton(MmsIcons.getLeftIcon());
  JButton nextMonth=new JButton(MmsIcons.getRightIcon());
  final Calendar now=Calendar.getInstance();

  public ShowCalendar(
	  final Frame parent,
	  final Calendar c,
	  final java.util.Date value){
	super(parent, true);
	final String title, syear, smonth;
	c.setTime(value);
	currentYear=IYEAR=c.get(Calendar.YEAR);
	currentMonth=IMONTH=c.get(Calendar.MONTH) + 1;
	currentDay=c.get(Calendar.DATE);
	syear=String.valueOf(IYEAR);
	smonth=String.valueOf(IMONTH);

	setSize(190, 235);
	getContentPane().add(calendarHeader, java.awt.BorderLayout.NORTH);
	getContentPane().add(calendarBody);
	getContentPane().add(djp3, java.awt.BorderLayout.SOUTH);
	djp3.setLayout(new FlowLayout());

	djp3.add(previousMonth);
	previousMonth.setMargin(new Insets(5, 5, 5, 5));
	previousMonth.setBorder(BorderFactory.createRaisedBevelBorder());
	previousMonth.addActionListener(new ActionListener(){
	  public void actionPerformed(ActionEvent e){
		calendarHeader.removeAll();
		calendarHeader.updateUI();
		calendarBody.removeAll();
		calendarBody.updateUI();
		IMONTH=IMONTH - 1;
		if (IMONTH < 1){
		  IMONTH=12;
		  IYEAR=IYEAR - 1;
		}
		cal_main(Integer.toString(IYEAR), Integer.toString(IMONTH));

	  }

	});

	djp3.add(today);

	djp3.add(nextMonth);
	nextMonth.addActionListener(new ActionListener(){
	  public void actionPerformed(ActionEvent e){
		calendarHeader.removeAll();
		calendarHeader.updateUI();
		calendarBody.removeAll();
		calendarBody.updateUI();

		IMONTH=IMONTH + 1;
		if (IMONTH > 12){
		  IMONTH=1;
		  IYEAR=IYEAR + 1;
		}
		cal_main(Integer.toString(IYEAR), Integer.toString(IMONTH));

	  }

	});
	nextMonth.setMargin(new Insets(5, 5, 5, 5));
	nextMonth.setBorder(BorderFactory.createRaisedBevelBorder());

	cal_main(syear, smonth);

	addWindowListener(new java.awt.event.WindowAdapter(){
	  public void windowClosing(java.awt.event.WindowEvent evt){
		dispose();
	  }
	});
  }

  int currentYear, currentMonth, currentDay, presentDay, presentMonth, presentYear;
  
  private void cal_main(final String syear, final String smonth){
	String stitle;
	stitle=smonth + "/" + syear;
	this.setTitle(stitle);
	int tmp_year=Integer.parseInt(syear);
	int tmp_month=Integer.parseInt(smonth);
	int the_first_day=0, end=0;
	boolean flag=true;

	if (tmp_year <= 0 || tmp_year >= 30000){
	  flag=false;
	}

	if (tmp_month < 1 || tmp_month > 12){
	  flag=false;
	}

	if (flag){
	  if (tmp_year == 1752 && tmp_month == 9){
		;
	  }
	  else{
		the_first_day=count_first_day(tmp_year, tmp_month);
		end=count_days(tmp_year, tmp_month);
		pri_cal(the_first_day, end,
				tmp_year == currentYear && tmp_month == currentMonth);
	  }
	}
   }

  void pri_cal(final int ifirst_day, final int iend,
			   final boolean hasCurrentDay){
	int i, j, irow;
	boolean bflag=false; /*GridLayout */

	String[] w_name={
		"Sun", "Mon", "Tue", "Wed", "Thu", "Fri", "Sat"
	};
	for (i=0; i < w_name.length; ++i){
	  final Label label=new Label(w_name[i], Label.CENTER);
	  label.setBackground(Color.blue);
	  label.setForeground(Color.white);
	  calendarHeader.add(label);
	}
	for (i=0; i < ifirst_day; ++i){
	  final Label label=new Label(" ", Label.CENTER);
	  label.setBackground(Color.white);
	  calendarHeader.add(label);
	}
	if (i > 0){
	  irow=(iend - (7 - ifirst_day)) / 7;
	  if (((iend - (7 - ifirst_day)) % 7) != 0){
		++
			irow;
	  }
	  ++irow;
	}
	else{
	  irow=iend / 7;

	  if ((iend % 7) != 0){
		++irow;
		if (iend == 29) {
		  bflag=true; /*GridLayout */
		}
	  }
	}
	if (bflag) {
	  calendarHeader.setLayout(new GridLayout(5, 7, 1, 1));
	}
	else{
	  calendarHeader.setLayout(new GridLayout(irow + 1, 7, 1, 1));
	}
	if (bflag){ /*GridLayout */
	  for (j=1; j <= 28; ++j){
		final Label dl=new Label(Integer.toString(j), Label.CENTER);
		if (hasCurrentDay){
		  if (currentDay == j){
			dl.setForeground(Color.RED);

		  }
		}

		calendarHeader.add(dl);
		dl.setBackground(Color.white);
		dl.setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));
		dl.addMouseListener(new MouseHandler(dl));
	  }
	  calendarBody.setLayout(new GridLayout(2, 7, 1, 1));
	  for (i=0; i < 14; ++i){
		final Label dl;
		if (i == 0){
		  dl=new Label("29", Label.CENTER);
		  dl.setBackground(Color.white);
		  dl.setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));
		  dl.addMouseListener(new MouseHandler(dl));
		}
		else{
		  dl=new Label(" ", Label.CENTER);
		}
		calendarBody.add(dl);
	  }
	}
	else{
	  for (j=1; j <= iend; ++j){
		final Label label=new Label(Integer.toString(j), Label.CENTER);
		label.setBackground(Color.white);
		presentDay = (int) now.get(Calendar.DATE);
		presentMonth = (int) now.get(Calendar.MONTH) + 1;
		presentYear = (int) now.get(Calendar.YEAR);
		if (hasCurrentDay){
		  if (currentDay == j){

			label.setForeground(Color.RED);
			label.setBackground(Color.LIGHT_GRAY);
			label.setFont(new Font(SwingBasics.FONT_FACE_FAVORITE.getFontName(),
								   Font.BOLD | Font.ITALIC, SwingBasics.FONT_FACE_FAVORITE.getSize()+1));

		  }
		}		
		else if(presentDay == j && presentMonth == IMONTH && presentYear == IYEAR) {
			label.setForeground(Color.RED);
			label.setBackground(Color.LIGHT_GRAY);
			label.setFont(new Font(SwingBasics.FONT_FACE_FAVORITE.getFontName(),
								   Font.BOLD | Font.ITALIC, 14));
		}
		calendarHeader.add(label);
		label.setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));
		label.addMouseListener(new MouseHandler(label));
		SwingUtilities.invokeLater(new Runnable() {
			public void run() {
				calendarHeader.updateUI();
				calendarBody.updateUI();
			}
		});
	  }
	}
  }

  class MouseHandler
	  extends MouseAdapter{
	final String sday_tmp;
	private final Label label;
	public MouseHandler(final Label label){
	  sday_tmp=label.getText();
	  this.label=label;
	}

	public void mouseEntered(MouseEvent me){
	  label.setBackground(Color.cyan);
	}

	public void mouseExited(MouseEvent me){
	  label.setBackground(Color.white);
	}

	public void mouseClicked(MouseEvent evt){
	  Calendar c=Calendar.getInstance();
	  c.set(Calendar.YEAR, IYEAR);
	  c.set(Calendar.MONTH, IMONTH - 1);
	  c.set(Calendar.DAY_OF_MONTH, Integer.parseInt(sday_tmp));
	  rvalue=c.getTime();
	  dispose();
	}
  }

  int count_first_day(int iyear, int imonth){
	int i, j, f_day=0;
	if (iyear < 1752 || (iyear == 1752 && imonth <= 8)){
	  f_day=6;
	  for (i=1; i < iyear; ++i){
		if (i % 4 == 0) {
		  f_day=(f_day + 366 % 7) % 7;
		}
		else{
		  f_day=(f_day + 365 % 7) % 7;
		}
	  }
	  for (j=1; j < imonth; ++j){
		f_day=(f_day + count_days(iyear, j)) % 7;
	  }
	}
	else if (iyear > 1752){
	  f_day=1;
	  for (i=1753; i < iyear; ++i){
		if ((i % 4 == 0 && i % 100 != 0) || i % 400 == 0) {
		  f_day=(f_day + 366 % 7) % 7;
		}
		else{
		  f_day=(f_day + 365 % 7) % 7;
		}
	  }
	  for (j=1; j < imonth; ++j) {
		f_day=(f_day + count_days(iyear, j)) % 7;
	  }
	}
	else if (iyear == 1752 && imonth >= 10){
	  f_day=0;
	  for (j=10; j < imonth; ++j) {
		f_day=(f_day + count_days(iyear, j)) % 7;
	  }
	}
	return (f_day);
  }

  int count_days(int iyear, int imonth){
	int days;
	if (iyear <= 1752 && iyear % 4 == 0 && imonth == 2) {
	  days=29;
	}
	else if (iyear > 1752 && ((iyear % 4 == 0 && iyear % 100 != 0) ||
							  iyear % 400 == 0) && imonth == 2) {
	  days=29;
	}
	else if (imonth == 1 || imonth == 3 || imonth == 5 || imonth == 7 ||
			 imonth == 8 || imonth == 10 || imonth == 12) {
	  days=31;
	}
	else if (imonth == 4 || imonth == 6 || imonth == 9 || imonth == 11) {
	  days=30;
	}
	else{
	  days=28;
	}
	return (days);
  }

}
