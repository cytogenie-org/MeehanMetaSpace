package com.MeehanMetaSpace.swing;

import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Dimension;
import java.awt.FlowLayout;
import java.awt.Point;
import java.awt.event.ActionListener;
import java.util.Observable;
import java.util.Observer;

import javax.swing.Icon;
import javax.swing.JButton;
import javax.swing.JFrame;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.ScrollPaneConstants;
import java.awt.Color;

public class ToolbarScrollablePanel extends JPanel  {

	private int totalheight, y;
	private Point point;
	private final int SCROLL_HEIGHT = 30;
	private JPanel toolbar;
	private ActionListener callBackListener;
	private JButton btnUp;
	private JButton btnDown;
	private JPanel jPanel;
	private JScrollPane jScrollPane1;
	private Color bgColor;
	public Color getBackground() {
		return bgColor;
	}

	public void setOpaque(final boolean b) {
		super.setOpaque(true);
	}

	public ToolbarScrollablePanel(JPanel toolbar,
			ActionListener callBackListener, Icon up, Icon down,
			boolean addControls,
			final Color bgColor) {
		super();
		this.bgColor=bgColor;
		this.callBackListener = callBackListener;
		initComponents(up, down, addControls);
		this.toolbar = toolbar;
		point = new Point(0, 0);
		jPanel.add(toolbar, BorderLayout.NORTH);
	}

	private void initComponents(Icon up, Icon down, boolean addControls) {

		btnUp = new JButton();
		btnDown = new JButton();
		jScrollPane1 = new JScrollPane();
		setOpaque(true);
		setBackground(bgColor);
		jPanel = new JPanel(){
			public Color getBackground() {
				return bgColor;
			}

			public void setOpaque(final boolean b) {
				super.setOpaque(true);
			}
		};

		setLayout(new java.awt.BorderLayout());

		if (up == null) {
			btnUp.setText("<");
		} else {
			btnUp.setIcon(up);
		}
		btnUp.addActionListener(new java.awt.event.ActionListener() {
			public void actionPerformed(java.awt.event.ActionEvent evt) {
				scrollDown(evt);
			}
		});
		JPanel northPanel = new JPanel(){
			public Color getBackground() {
				return bgColor;
			}

			public void setOpaque(final boolean b) {
				super.setOpaque(true);
			}
		};
		northPanel.setLayout(new java.awt.BorderLayout());
		northPanel.add(btnUp, java.awt.BorderLayout.EAST);

		if (down == null) {
			btnDown.setText(">");
		} else {
			btnDown.setIcon(down);
		}

		btnDown.addActionListener(new java.awt.event.ActionListener() {
			public void actionPerformed(java.awt.event.ActionEvent evt) {
				scrollUp(evt);
			}
		});
		northPanel.add(btnDown, java.awt.BorderLayout.WEST);

		if (addControls) {
			add(northPanel, java.awt.BorderLayout.NORTH);
		}

		jScrollPane1.setHorizontalScrollBarPolicy(ScrollPaneConstants.HORIZONTAL_SCROLLBAR_NEVER);
		jScrollPane1.setVerticalScrollBarPolicy(ScrollPaneConstants.VERTICAL_SCROLLBAR_AS_NEEDED);
		jPanel.setLayout(new java.awt.BorderLayout());

		add(jPanel, java.awt.BorderLayout.CENTER);
	}

	private void scrollUp(java.awt.event.ActionEvent evt) {
		if (totalheight <= (y + jScrollPane1.getVisibleRect().height)) {
		} else {
			y += SCROLL_HEIGHT;
			point.y = y;
			jScrollPane1.getViewport().setViewPosition(point);
		}
	}

	private void scrollDown(java.awt.event.ActionEvent evt) {
		if (y != 0) {
			y -= SCROLL_HEIGHT;
			point.y = y;
			jScrollPane1.getViewport().setViewPosition(point);
		}
	}

	public void update() {
		totalheight = toolbar.getBounds().height;
		if (getBounds().height > totalheight) {
			btnUp.setVisible(true);
			btnDown.setVisible(true);
		} else {
			btnUp.setVisible(true);
			btnDown.setVisible(true);
			y = 0;
		}
		validate();
	}

	public JButton getBtnUp() {
		return btnUp;
	}

	public JButton getBtnDown() {
		return btnDown;
	}
	
	public static void main(String a[]) {
		JPanel panel = new JPanel();
		panel.setLayout(new FlowLayout());

		for (int i = 0; i < 50; i++) {
			panel.add(new JButton("Butt " + i));
		}

		ToolbarScrollablePanel tool = new ToolbarScrollablePanel(panel, null,
				null, null, true, Color.white);
		JFrame frame = new JFrame("Hi");
		frame.add(tool);
		frame.setSize(new Dimension(500, 500));
		frame.setVisible(true);

	}
}
