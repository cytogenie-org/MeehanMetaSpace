/*
 =====================================================================
  JPersonalizedTableTest.java
  Created by Stephen Meehan
  Copyright (c) 2002
 =====================================================================
 */
package com.MeehanMetaSpace.swing;

import java.io.*;
import java.util.*;

import java.awt.*;
import java.awt.event.*;
import javax.swing.*;
import javax.swing.table.*;

import com.MeehanMetaSpace.*;

public class TestTreeEditRule
	 extends JPanel{

	PersonalizableTable table;

	public TestTreeEditRule(){
		setLayout(new GridLayout());
		setBorder(BorderFactory.createEmptyBorder(4, 4, 4, 4));
		makeModel();
		table=new PersonalizableTable(tableModel);

		table.prepareForNestedTables(
			 new NestedTableCellEditor.Table(
			 new Object[][]{{null, null}
			 , {null, null}
		}){
			public boolean isCellEditable(int row, int visualColumn){
				int modelColumn=SwingBasics.getModelIndexFromVisualIndex(this, visualColumn);
				return modelColumn==1; // can only edit cocktail volume, NOT dilution or step
			}
		});
		add(table.makeHorizontalScrollPane());
		tableModel.setAutoFilter(true);

	}

	final static int
		 colTissue=0,
		 colSubject=1,
		 colCellsTotalMl=2,
		 colCellDensity=3,
		 colStainSet=4,
		 colIncludeInArray=5,
		 colRepeats=6,
		 colCellsUlToAdd=7,
		 colStainStepVolumes=8,
		 colFmo=9,
		 colId=10,
		 colCnt=11;

	final static String[] colNames=
		 new String[]{
		 "Cell sources",
		 "Subject",
		 "Cells total ul",
		 "Cells/ml X 10^6",
		 "Stain set",
		 "Include in assay",
		 "Repeats",
		 "Cells ul to add",
		 "Stain volumes",
		 "FMO",
		 "Id"
	};

	final static String[] colLabels=
		 new String[]{
		 "Cell sources",
		 "Subject",
		 "<html><body><center>Cell<br>sample<br>total ul</center></body></html>",
		 "<html><body><center>Cells/ml<br> X 10^6</center></body></html>",
		 "Stain set",
		 "<html><body><center>Include<br>in assay</center></body></html>",
		 "<html><body><center>Repeats</center></body></html>",
		 "<html><body><center>Cell<br>sample<br>ul to add</center></body></html>",
		 "<html><body><center><b>Stain</b><table border='1'><tr><td>step</td><td>volume (ul)</td></tr></table></center></body></html>",
		 "<html><body><center>Fluoresence minus one<br>(FMO)<br><i>each color is a separate tube</i></center></body></html>",
		 null
	};

	final String fmoValues[]={
		 "CD21/<PE>",
		 "CD4/<FITC>",
		 "B220/<TR>",
		 "CD25/<Cy5.PE>",
		 "CD8/<Cy7>",
		 "IgM/<CasBlu>",
		 "IgD/<Cychrome>"};
	static ClassLoader cl=TestTreeEditRule.class.getClassLoader();
	private static final Icon
		 tissueIcon=new ImageIcon(cl.getResource("com/MeehanMetaSpace/swing/images/heart.gif")),
		 noTissueIcon=new ImageIcon(cl.getResource("com/MeehanMetaSpace/swing/images/noHeart.gif")),
		 mouseIcon=new ImageIcon(cl.getResource("com/MeehanMetaSpace/swing/images/people.gif")),
		 noMouseIcon=new ImageIcon(cl.getResource("com/MeehanMetaSpace/swing/images/noPeople.gif")),
		 stainIcon=new ImageIcon(cl.getResource("com/MeehanMetaSpace/swing/images/heart.gif")),
		 spectrumIcon=new ImageIcon(cl.getResource("com/MeehanMetaSpace/swing/images/help24.gif"));

	Vector makeFakeData(){
		Random random=new Random();

		final String tissueValues[]={
			 "Spleen", "Peritoneal cavity", "Bone marrow",
			 "Thoracic duct", "Lymph node", "Thymus"};
		final String subjectValues[]={
			 "Mickey Mouse", "Stuart Little",
			 "Jerry", "Minnie Mouse", "Repecheep", "Fievel Mousekewitz",
			 "Pinky", "Mighty Mouse", "Bernard", "Itchy", "Speedy Gonzalez"};
		final Float totalCellsValues[][]={
			 {
			 new Float(1.25), new Float(1.75), new Float(2), new Float(2.5), new Float(4.2), new Float(1.2)}
			 , {
			 new Float(2.25), new Float(2.75), new Float(3), new Float(4.5), new Float(3.2), new Float(0.95)}
			 , {
			 new Float(3.25), new Float(3.75), new Float(4), new Float(3.35), new Float(2.2), new Float(1.25)}
			 , {
			 new Float(4.25), new Float(4.75), new Float(5), new Float(5.45), new Float(1.2), new Float(2.35)}
			 , {
			 new Float(5.25), new Float(5.75), new Float(1), new Float(1.55), new Float(5.2), new Float(1.88)}
			 , {
			 new Float(0.25), new Float(0.75), new Float(2.1), new Float(2.6), new Float(7.5), new Float(2.99)}
			 , {
			 new Float(1.35), new Float(1.85), new Float(3.1), new Float(4.6), new Float(6.2), new Float(4.2)}
			 , {
			 new Float(2.35), new Float(2.85), new Float(4.1), new Float(3.6), new Float(4.5), new Float(4.2)}
			 , {
			 new Float(3.35), new Float(3.85), new Float(5.1), new Float(5.6), new Float(4.2), new Float(4.2)}
			 , {
			 new Float(4.35), new Float(4.85), new Float(1.1), new Float(1.6), new Float(4.5), new Float(4.2)}
			 , {
			 new Float(5.35), new Float(5.85), new Float(1.9), new Float(2.66), new Float(4.2), new Float(4.2)}
		};
		final String[] stainSetValues=new String[]{
			 "Mature B",
			 "Lineage",
			 "Lymphocyte lineage",
			 "Mature T"
		};
		final Float totalCellSuspensionMlValues[][]={
			 {
			 new Float(4.25), new Float(4.75), new Float(5), new Float(5.45), new Float(1.2), new Float(2.35)}
			 , {
			 new Float(5.25), new Float(5.75), new Float(1), new Float(1.55), new Float(5.2), new Float(1.88)}
			 , {
			 new Float(0.25), new Float(0.75), new Float(2.1), new Float(2.6), new Float(7.5), new Float(2.99)}
			 , {
			 new Float(1.35), new Float(1.85), new Float(3.1), new Float(4.6), new Float(6.2), new Float(4.2)}
			 , {
			 new Float(2.35), new Float(2.85), new Float(4.1), new Float(3.6), new Float(4.5), new Float(4.2)}
			 , {
			 new Float(3.35), new Float(3.85), new Float(5.1), new Float(5.6), new Float(4.2), new Float(4.2)}
			 , {
			 new Float(4.35), new Float(4.85), new Float(1.1), new Float(1.6), new Float(4.5), new Float(4.2)}
			 , {
			 new Float(5.35), new Float(5.85), new Float(1.9), new Float(2.66), new Float(4.2), new Float(4.2)}
			 , {
			 new Float(1.25), new Float(1.75), new Float(2), new Float(2.5), new Float(4.2), new Float(1.2)}
			 , {
			 new Float(2.25), new Float(2.75), new Float(3), new Float(4.5), new Float(3.2), new Float(0.95)}
			 , {
			 new Float(3.25), new Float(3.75), new Float(4), new Float(3.35), new Float(2.2), new Float(1.25)}
		};
		final Float volumeFakeValues[]={
			 new Float(25.0),
			 new Float(17.5),
			 new Float(32.5)
		};
		final Vector data=new Vector();
		int tubePlans=0;
		for (int tissue=0; tissue<tissueValues.length; tissue++){

			for (int subject=0; subject<subjectValues.length; subject++){
				for (int stainSet=0; stainSet<stainSetValues.length; stainSet++){
					int idx;
					final Vector row=new Vector();
					for (int j=0; j<colCnt; j++){
						switch (j){
						case colId:
							row.add(new Integer(++tubePlans));
							break;
						case colTissue:
							row.add(tissueValues[tissue]);
							break;
						case colSubject:
							row.add(subjectValues[subject]);
							break;
						case colCellsTotalMl:
							row.add(totalCellSuspensionMlValues[subject][tissue]);
							break;
						case colCellDensity:
							row.add(totalCellsValues[subject][tissue]);

							break;
						case colStainSet:
							row.add(stainSetValues[stainSet]);
							break;
						case colIncludeInArray:
							row.add(ComparableBoolean.valueOf(random.nextBoolean()));
							break;
						case colCellsUlToAdd:
							idx=random.nextInt(volumeFakeValues.length);
							row.add(volumeFakeValues[idx]);
							break;
						case colRepeats:
							int seed=random.nextInt(3);
							row.add(new Integer( (seed%3)));
							break;
						case colStainStepVolumes:
							row.add(new Object[][]{{
									  "1", new Integer(25)}
									  , {
									  "2", new Integer(44)}
									  ,
							}
								 );
							break;
						case colFmo:
							idx=random.nextInt(fmoValues.length);
							row.add(fmoValues[idx]);
							break;
						}
					}
					data.add(new ListRow(row){
						public boolean isEditable(int dataColumn){
							switch (dataColumn){
							case colId:
							case colStainSet:
							case colSubject:
							case colTissue:
								return false;
							}
							return true;
						}

						public boolean setAdvice(int dataColumn, CellAdvice cellAdvice){
							if (dataColumn==colCellDensity){
								Number n= (Number) row.get(dataColumn);
								if (n.intValue()==0){
									cellAdvice.set(CellAdvice.TYPE_ERROR, "Total cells can not be zero");
									return true;

								}
							}
							if (dataColumn==colCellsTotalMl){
								Number n= (Number) row.get(dataColumn);
								if (n.doubleValue()==0){
									cellAdvice.set(CellAdvice.TYPE_ERROR, "Total cell suspsensio can not be zero");
									return true;

								}
							}
							return false;
						}

						public Collection getAllowedValues(int dataColumnIndex){
							if (dataColumnIndex==colTissue){
								return Basics.toList(tissueValues);
							}
							return null;
						}

						public MetaRow getMetaRow(){
							return metaRow;
						}

					});
				}
			}
		}

		return data;
	}

	ListMetaRow metaRow;

	static PersonalizableTableModel.MultiRowEditRule
		 ruleCellDensity, ruleTotalCellMl, ruleCellsUlToAdd, ruleSampleRepeats,
		 ruleStainVolume,
		 ruleFmo, ruleInclude;

	PersonalizableTableModel makeModel(){
		final Vector data=makeFakeData();
		metaRow=new ListMetaRow(Arrays.asList(colNames), (Row) data.get(0)){
			public String getLabel(int dataColumnIndex){
				return colLabels[dataColumnIndex];
			}

			public Icon getIcon(
						 final Iterator selectedRows,
						 final int dataColumn,
						 final boolean isExpanded,
						 final boolean isLeaf){
				switch (dataColumn){
				case colSubject:
					if (TableBasics.hasZero(
						 selectedRows,
						 new int[]{colCellsTotalMl, colCellDensity})){
						return noMouseIcon;
					}
					return mouseIcon;
				case colTissue:
					if (TableBasics.hasZero(
						 selectedRows,
						 new int[]{colCellsTotalMl, colCellDensity})){
						return noTissueIcon;
					}
					return tissueIcon;
				}

				return null;
			}

			public Icon getIcon(int dataColumn){
				if (dataColumn==colStainSet){
					return stainIcon;
				}
				return null;
			}
		};

		final StringConverter fmoSc=new DefaultStringConverters._String(){
			public String toString(Object input){
				return "my "+super.toString(input);
			}
		};

		final int randomnBadFmo=3;
		dataSource=new DefaultPersonalizableDataSource(data, metaRow){
			public boolean handleDoubleClick(final TableCellContext context){
				return false;
			}

			public TableCellEditor getCellEditor(
				 final Row row,
				 final int dataColumnIndex){
				if (dataColumnIndex==colFmo){
					final SelectableCell cml=new SelectableCell.Default(
						 (StringConverter)null,
						 row,
						 dataColumnIndex,
						 ',',
						 fmoValues
						 ){
						public boolean isViewable(){
							return true;
						}

						protected boolean allowsDuplicates(){
							return false;
						}

						protected String getAddAnomaly(final Object value){
							if (value.equals(fmoValues[randomnBadFmo])){
								return "Don't like this one for some odd reason";
							}
							return null;
						}
					};
					final TableCellEditor tce=cml.getTableCellEditor();
					if (tce!=null){
						return tce;
					}
				}
				return super.getCellEditor(row, dataColumnIndex);
			}
		};

		if (!new File(getPropertyFileName()).exists()){
			try{
				IoBasics.copy(
						getPropertyFileName(),
						"http://www.MeehanMetaSpace.com//FacsXpertAlpha21//"+getPropertyFileName(),
						null);
			} catch (IOException e){
				Pel.log.warn(e);
			}
		}
		tableModel=PersonalizableTableModel.activate(dataSource, getPropertyFileName(), false);
		tableModel.setSelectionsPropagateToLeaf(true);
		tableModel.setKey("tube plan");


		ruleCellDensity=tableModel.new MultiRowEditRule(
			 MultiRowEditRuleType.SELECTED_AND_TOP_OF_SORT,
			 null,
			 "<html><body>Tissue or subject <b>MUST</b> be selected and top nodes of tree</body></html>",
			 new int[]{colCellDensity}
			 ,
			 new int[]{colTissue, colSubject}
			 );

		ruleTotalCellMl=tableModel.new MultiRowEditRule(
			 MultiRowEditRuleType.SELECTED_AND_TOP_OF_SORT,
			 null,
			 "<html><body>Tissue or subject <b>MUST</b> be selected and top nodes of tree</body></html>",
			 new int[]{colCellsTotalMl}
			 ,
			 new int[]{colTissue, colSubject}
			 );

		ruleSampleRepeats=tableModel.new MultiRowEditRule(
			 null,
			 "",
			 new int[]{colRepeats});

		ruleCellsUlToAdd=tableModel.new MultiRowEditRule(
			 null,
			 "",
			 new int[]{colCellsUlToAdd});

		ruleStainVolume=tableModel.new MultiRowEditRule(
			 MultiRowEditRuleType.SELECTED_OR_COMMON_PARENT
			 ,
			 null
			 ,
			 "<html><body>All selections <b>MUST</b>apply to the same stain set</body></html>"
			 ,
			 new int[]{colStainStepVolumes}
			 ,
			 new int[]{colStainSet});

		ruleInclude=tableModel.new MultiRowEditRule(
			 MultiRowEditRuleType.SELECTED_OR_COMMON_PARENT
			 ,
			 null
			 ,
			 "<html><body>All selections <b>MUST</b>apply to the same stain set</body></html>"
			 ,
			 new int[]{colIncludeInArray}
			 ,
			 new int[]{colStainSet});

		ruleFmo=tableModel.new MultiRowEditRule(
			 MultiRowEditRuleType.SELECTED_OR_COMMON_PARENT
			 ,
			 null
			 ,
			 "<html><body>All selections <b>MUST</b>apply to the same stain set</body></html>"
			 ,
			 new int[]{colFmo}
			 ,
			 new int[]{colStainSet});

		return tableModel;
	}

	DefaultPersonalizableDataSource dataSource;

	String getPropertyFileName(){
		final String s="testTreeEditRule3.properties";
		System.out.println("Loading "+new java.io.File(s).getAbsolutePath());
		return s;
	}

	static boolean testingDialog=false;

	PersonalizableTableModel tableModel;

	void saveProperties(){
		if (tableModel.getViewChangeCount()>0){
			Properties prop=tableModel.updatePropertiesWithPersonalizations(true);
			PropertiesBasics.saveProperties(prop, getPropertyFileName(), "");
		}
	}

	public static void main(final String[] args){

		try{
			// start program event logging
			Pel.init(null, TestTreeEditRule.class, "Test Tree Edit Rule", false);
			UIManager.setLookAndFeel(UIManager.getSystemLookAndFeelClassName());
		} catch (Exception e){
			e.printStackTrace();
		}
		final TestTreeEditRule testTreeEditRule=new TestTreeEditRule();
		final Font buttonFont=new Font("Arial", Font.ITALIC, 12);
		final JFrame frame=new JFrame("Experiment plan");
		final GridLayout gridLayout=new GridLayout(1, 6);
		gridLayout.setHgap(11);
		JPanel buttons=new JPanel(gridLayout);

		final JLabel icon=new JLabel("    ");
		icon.setIcon(spectrumIcon);
		icon.setHorizontalTextPosition(JLabel.LEFT);
		buttons.add(icon);

		final JButton sampleProperties=
			 new JButton("<html><body><center>Sample<br>properties</center></body></html>", mouseIcon);
		final JButton samplePipetting=
			 new JButton("<html><body><center>Sample<br>pipetting</center></body></html>", tissueIcon);
		final JButton stainSetPipetting=
			 new JButton("<html><body><center>Stain set<br>pipetting</center></body></html>", stainIcon);
		final JButton stainSetProperties=
			 new JButton("<html><body><center>Stain set<br>properties</center></body></html>");

		sampleProperties.setMnemonic('s');
		sampleProperties.setToolTipText("Obtain tree view that assists in editing sample quantities for entire protocol");
		sampleProperties.setFont(buttonFont);

		ActionListener samplePropertiesAction=new ActionListener(){
			public void actionPerformed(ActionEvent e){
				sampleProperties.setEnabled(false);
				samplePipetting.setEnabled(true);
				stainSetPipetting.setEnabled(true);

				// handle menu items on tree popup menu under Edit
				ruleCellDensity.menuItem.setVisible(true);
				ruleTotalCellMl.menuItem.setVisible(true);
				ruleCellsUlToAdd.menuItem.setVisible(false);
				ruleSampleRepeats.menuItem.setVisible(false);
				ruleStainVolume.menuItem.setVisible(false);
				ruleFmo.menuItem.setVisible(false);
				ruleInclude.menuItem.setVisible(false);

				testTreeEditRule.tableModel.group(
					 new int[]{colTissue, colSubject}
					 ,
					 new int[]{colTissue, colSubject, colCellsTotalMl, colCellDensity}
					 ,
					 true);
			}
		};
		sampleProperties.addActionListener(samplePropertiesAction);
		samplePipetting.setMnemonic('p');
		samplePipetting.setToolTipText("Obtain view that supports editing sample volumes per tube");
		samplePipetting.setFont(buttonFont);
		samplePipetting.addActionListener(new ActionListener(){
			public void actionPerformed(ActionEvent e){
				sampleProperties.setEnabled(true);
				samplePipetting.setEnabled(false);
				stainSetPipetting.setEnabled(true);

				// handle menu items on tree popup menu under Edit
				ruleCellDensity.menuItem.setVisible(false);
				ruleTotalCellMl.menuItem.setVisible(false);
				ruleCellsUlToAdd.menuItem.setVisible(true);
				ruleSampleRepeats.menuItem.setVisible(true);
				ruleStainVolume.menuItem.setVisible(false);
				ruleFmo.menuItem.setVisible(false);
				ruleInclude.menuItem.setVisible(false);

				testTreeEditRule.tableModel.group(
					 new int[]{colSubject, colTissue, colStainSet}
					 ,
					 new int[]{colSubject, colTissue, colStainSet, colRepeats, colCellsUlToAdd}
					 ,
					 false
					 );
			}
		});

		stainSetPipetting.setMnemonic('t');
		stainSetPipetting.setToolTipText("Obtain view that supports editing cocktail volumes per tube");
		stainSetPipetting.setFont(buttonFont);
		stainSetPipetting.addActionListener(new ActionListener(){
			public void actionPerformed(ActionEvent e){
				sampleProperties.setEnabled(true);
				samplePipetting.setEnabled(true);
				stainSetPipetting.setEnabled(false);

				// handle menu items on tree popup menu under Edit
				ruleCellDensity.menuItem.setVisible(false);
				ruleTotalCellMl.menuItem.setVisible(false);
				ruleCellsUlToAdd.menuItem.setVisible(false);
				ruleSampleRepeats.menuItem.setVisible(false);
				ruleStainVolume.menuItem.setVisible(true);
				ruleFmo.menuItem.setVisible(true);
				ruleInclude.menuItem.setVisible(true);


				testTreeEditRule.tableModel.group(
					 new int[]{colStainSet, colSubject, colTissue}
					 ,
					 new int[]{colStainSet, colSubject, colTissue, colIncludeInArray, colFmo, colStainStepVolumes}
					 ,
					 false);
			}
		});

		stainSetProperties.setMnemonic('e');
		stainSetProperties.setToolTipText("Obtain view that supports editing cocktail volumes per tube");
		stainSetProperties.setFont(buttonFont);
		stainSetProperties.addActionListener(new ActionListener(){
			public void actionPerformed(ActionEvent e){
				PopupBasics.alert("In FacsXpert this would go to the reagent pick tree (this is just a prototype)");
			}
		});
		final JButton protocolProperties=new JButton(
				  "<html><body><center>Protocol<br>properties</center></body></html>");
		protocolProperties.setMnemonic('d');
		protocolProperties.setFont(buttonFont);
		protocolProperties.addActionListener(new ActionListener(){
			public void actionPerformed(ActionEvent e){
				PopupBasics.alert("In FacsXpert this would return to the front protocol form");
			}
		});
		buttons.add(sampleProperties);
		buttons.add(samplePipetting);
		buttons.add(stainSetPipetting);
		buttons.add(protocolProperties);
		buttons.add(SwingBasics.getNextButton(
								 "Go to comfort level definitions",
								 new ActionListener(){
								public void actionPerformed(ActionEvent e){
									SwingBasics.closeWindow(frame);
								}
							}));

		frame.getContentPane().setLayout(new BorderLayout());
		frame.getContentPane().add(buttons, BorderLayout.NORTH);
		frame.getContentPane().add(testTreeEditRule, BorderLayout.CENTER);
		frame.addWindowListener(new WindowAdapter(){
			public void windowClosing(WindowEvent e){
				testTreeEditRule.saveProperties();
				System.exit(0);
			}
		});

		JPanel jp=new JPanel();
		JLabel s=new JLabel();
		jp.add(s);
		testTreeEditRule.tableModel.setSizeInfo(s);
		frame.getContentPane().add(jp, BorderLayout.SOUTH);
		frame.pack();
		testTreeEditRule.tableModel.setPersonalizableWindowOwner( (Window) frame);
		samplePropertiesAction.actionPerformed(null);
		frame.setIconImage(java.awt.Toolkit.getDefaultToolkit().getImage(
			 TestTreeEditRule.class.getClassLoader().getResource("com/MeehanMetaSpace/swing/images/spectrum.gif")));
		frame.show();

	}
}
