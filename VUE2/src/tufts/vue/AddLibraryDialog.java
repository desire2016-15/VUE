/*
 * -----------------------------------------------------------------------------
 *
 * <p><b>License and Copyright: </b>The contents of this file are subject to the
 * Mozilla Public License Version 1.1 (the "License"); you may not use this file
 * except in compliance with the License. You may obtain a copy of the License
 * at <a href="http://www.mozilla.org/MPL">http://www.mozilla.org/MPL/.</a></p>
 *
 * <p>Software distributed under the License is distributed on an "AS IS" basis,
 * WITHOUT WARRANTY OF ANY KIND, either express or implied. See the License for
 * the specific language governing rights and limitations under the License.</p>
 *
 * <p>The entire file consists of original code.  Copyright &copy; 2003, 2004
 * Tufts University. All rights reserved.</p>
 *
 * -----------------------------------------------------------------------------
 */
/*
 * AddEditDataSourceDialog.java
 * The UI to Add/Edit Datasources.
 * Created on June 8, 2004, 5:07 PM
 */

package tufts.vue;

/**
* @version $Revision: 1.24 $ / $Date: 2006-06-25 23:10:19 $ / $Author: jeff $
 * @author  akumar03
 */
import javax.swing.*;
import java.awt.event.*;

import javax.swing.event.*;
import java.awt.*;
import tufts.vue.gui.*;

public class AddLibraryDialog extends JDialog implements ListSelectionListener, ActionListener {
    
    JPanel addLibraryPanel = new JPanel();
    JList addLibraryList;
	JTextArea descriptionTextArea;
	DefaultListModel listModel = new DefaultListModel();
	JScrollPane listJsp;
	JScrollPane descriptionJsp;
	
	edu.tufts.vue.dsm.DataSourceManager dataSourceManager;
	edu.tufts.vue.dsm.OsidFactory factory;
	org.osid.provider.Provider checked[];
	java.util.Vector checkedVector = new java.util.Vector();
	JButton addButton = new JButton("Add");
	JButton cancelButton = new JButton("Done");
	JPanel buttonPanel = new JPanel();
	DataSourceList dataSourceList;
	DataSource oldDataSource = null;
	edu.tufts.vue.dsm.DataSource newDataSource = null;
	
	private static String MY_COMPUTER = "My Computer";
	private static String MY_COMPUTER_DESCRIPTION = "Add a browse control for your filesystem.  You can configure where to start the tree.";
	private static String MY_SAVED_CONTENT = "My Saved Content";
	private static String MY_SAVED_CONTENT_DESCRIPTION = "Add a browse control for your saved content.  You can configure a name for this source.";
	private static String FTP = "FTP";
	private static String FTP_DESCRIPTION = "Add a browse control for an FTP site.  You must configure this.";
	private static String TITLE = "Add a Resource";
	private static String AVAILABLE = "Resources available:";
    private final Icon remoteIcon = VueResources.getImageIcon("dataSourceRemote");
    private ProviderListCellRenderer providerListRenderer;
    private Timer timer;
    
    public AddLibraryDialog(DataSourceList dataSourceList)
	{
        super(VUE.getDialogParentAsFrame(),TITLE,true);
		this.dataSourceList = dataSourceList;
		
		try {
			factory = edu.tufts.vue.dsm.impl.VueOsidFactory.getInstance();
			addLibraryList = new JList(listModel);
			addLibraryList.setSelectionMode(DefaultListSelectionModel.SINGLE_SELECTION);
			addLibraryList.setPreferredSize(new Dimension(300,180));
			addLibraryList.addListSelectionListener(this);

			providerListRenderer = new ProviderListCellRenderer();
			addLibraryList.setCellRenderer(providerListRenderer);

			
			descriptionTextArea = new JTextArea();
			descriptionTextArea.setLineWrap(true);
			descriptionTextArea.setWrapStyleWord(true);
			descriptionTextArea.setPreferredSize(new Dimension(300,180));
			
			populate();
			
			listJsp = new JScrollPane(addLibraryList);
			listJsp.setVerticalScrollBarPolicy(javax.swing.JScrollPane.VERTICAL_SCROLLBAR_ALWAYS);
			listJsp.setHorizontalScrollBarPolicy(javax.swing.JScrollPane.HORIZONTAL_SCROLLBAR_ALWAYS);
			
			descriptionTextArea.setText("description");
			descriptionJsp = new JScrollPane(descriptionTextArea);
			descriptionJsp.setVerticalScrollBarPolicy(javax.swing.JScrollPane.VERTICAL_SCROLLBAR_AS_NEEDED);
			descriptionJsp.setHorizontalScrollBarPolicy(javax.swing.JScrollPane.HORIZONTAL_SCROLLBAR_AS_NEEDED); 
			
			addLibraryPanel.setBackground(VueResources.getColor("White"));
			setBackground(VueResources.getColor("White"));
			
			java.awt.GridBagLayout gbLayout = new java.awt.GridBagLayout();
			java.awt.GridBagConstraints gbConstraints = new java.awt.GridBagConstraints();
			gbConstraints.anchor = java.awt.GridBagConstraints.WEST;
			gbConstraints.insets = new java.awt.Insets(2,2,2,2);
			addLibraryPanel.setLayout(gbLayout);
			
			gbConstraints.gridx = 0;
			gbConstraints.gridy = 0;
			addLibraryPanel.add(new JLabel(AVAILABLE),gbConstraints);
			
			gbConstraints.gridx = 0;
			gbConstraints.gridy = 1;
			addLibraryPanel.add(listJsp,gbConstraints);
			
			gbConstraints.gridx = 0;
			gbConstraints.gridy = 2;
			addLibraryPanel.add(descriptionJsp,gbConstraints);
			
			buttonPanel.add(cancelButton);
			cancelButton.addActionListener(this);
			buttonPanel.add(addButton);
			addButton.addActionListener(this);			
			getRootPane().setDefaultButton(addButton);
			
			gbConstraints.gridx = 0;
			gbConstraints.gridy = 3;
			addLibraryPanel.add(buttonPanel,gbConstraints);
			
			getContentPane().add(addLibraryPanel,BorderLayout.CENTER);
			pack();
			setLocation(300,300);

			//setSize(new Dimension(300,400));
			
			//addLibraryList.getSelectionMdoel().setSelectionInterval(0,1);
		} catch (Throwable t) {
			t.printStackTrace();
		}
		setVisible(true);
    }
	
	public void refresh()
	{
		populate();
	}
	
    private void populate()
	{
		listModel.removeAllElements();
		try
		{
			GUI.activateWaitCursor();
			if (dataSourceManager == null) {
				dataSourceManager = edu.tufts.vue.dsm.impl.VueDataSourceManager.getInstance();
			}
			
			listModel.removeAllElements();
			org.osid.provider.ProviderIterator providerIterator = factory.getProviders();
			while (providerIterator.hasNextProvider()) {
				org.osid.provider.Provider nextProvider = providerIterator.getNextProvider();
				// place all providers on list, whether installed or not, whether duplicates or not
				listModel.addElement(nextProvider);
				descriptionTextArea.setText(nextProvider.getDescription());
				checkedVector.addElement(nextProvider);
			}
			// copy to an array
			int size = listModel.size();
			checked = new org.osid.provider.Provider[size];
			for (int i=0; i < size; i++) {
				checked[i] = (org.osid.provider.Provider)checkedVector.elementAt(i);
			}
			
		} catch (Throwable t) {
			t.printStackTrace();
			javax.swing.JOptionPane.showMessageDialog(null,
													  t.getMessage(),
													  "Error",
													  javax.swing.JOptionPane.ERROR_MESSAGE);
		} finally {
			GUI.clearWaitCursor();
		}
		// add all data sources we include with VUE
		listModel.addElement(MY_COMPUTER);
		listModel.addElement(MY_SAVED_CONTENT);
		listModel.addElement(FTP);
	}
	
	public void valueChanged(ListSelectionEvent lse) {				
		int index = ((JList)lse.getSource()).getSelectedIndex();
		if (index != -1) {
			try {
				if (((JList)lse.getSource()).getSelectedValue() instanceof String)
				{
					String s = (String)(((JList)lse.getSource()).getSelectedValue());
					if (s.equals(MY_COMPUTER)) {
						descriptionTextArea.setText(MY_COMPUTER_DESCRIPTION);
					} else if (s.equals(MY_SAVED_CONTENT)) {
						descriptionTextArea.setText(MY_SAVED_CONTENT_DESCRIPTION);
					} else if (s.equals(FTP)) {
						descriptionTextArea.setText(FTP_DESCRIPTION);
					}	
				}
				else {
					org.osid.provider.Provider p = (org.osid.provider.Provider)(((JList)lse.getSource()).getSelectedValue());
					descriptionTextArea.setText(p.getDescription());
				}
			} catch (Throwable t) {
				t.printStackTrace();
			}
		}
	}
	
	public void add() {
		
			try {
				this.oldDataSource = null;
				Object o = addLibraryList.getSelectedValue();
				String xml = null;
				String s = null;
				
				if (o instanceof String) {
					s = (String)o;
					if (s.equals(MY_COMPUTER)) {
						LocalFileDataSource ds = new LocalFileDataSource(MY_COMPUTER,"");
						xml = "<?xml version=\"1.0\" encoding=\"UTF-8\"?><configuration><field><key>name</key><title>Name</title><description>Name for this datasource</description><default>DEFAULT_NAME</default><mandatory>true</mandatory><maxChars></maxChars><ui>0</ui></field><field><key>address</key><title>Starting path</title><description>The path to start from</description><default>DEFAULT_ADDRESS</default><mandatory>true</mandatory><maxChars>512</maxChars><ui>0</ui></field></configuration>";
						String name = ds.getDisplayName();
						String address = ds.getAddress();
						xml = xml.replaceFirst("DEFAULT_NAME",name);
						xml = xml.replaceFirst("DEFAULT_ADDRESS",address);
						this.oldDataSource = ds;
					} else if (s.equals(MY_SAVED_CONTENT)) {
						FavoritesDataSource ds = new FavoritesDataSource(MY_SAVED_CONTENT);
						xml = "<?xml version=\"1.0\" encoding=\"UTF-8\"?><configuration><field><key>name</key><title>Name</title><description>Name for this datasource</description><default>DEFAULT_NAME</default><mandatory>true</mandatory><maxChars></maxChars><ui>0</ui></field></configuration>";
						String name = ds.getDisplayName();
						xml = xml.replaceFirst("DEFAULT_NAME",name);
						this.oldDataSource = ds;
					} else if (s.equals(FTP)) {
						RemoteFileDataSource ds = new RemoteFileDataSource();
						xml = "<?xml version=\"1.0\" encoding=\"UTF-8\"?><configuration><field><key>name</key><title>Display Name</title><description>Dane for this datasource</description><default>DEFAULT_NAME</default><mandatory>true</mandatory><maxChars></maxChars><ui>0</ui></field><field><key>address</key><title>Address</title><description>FTP Address</description><default>DEFAULT_ADDRESS</default><mandatory>true</mandatory><maxChars>256</maxChars><ui>0</ui></field><field><key>username</key><title>Username</title><description>FTP site username</description><default>DEFAULT_USERNAME</default><mandatory>true</mandatory><maxChars>64</maxChars><ui>0</ui></field><field><key>password</key><title>Password</title><description>FTP site password for username</description><default>DEFAULT_PASSWORD</default><mandatory>true</mandatory><maxChars></maxChars><ui>1</ui></field></configuration>";
						String name = ds.getDisplayName();
						if (name == null) name = "";
						String address = ds.getAddress();
						if (address == null) address = "";
						String username = ds.getUserName();
						if (username == null) username = "";
						String password = ds.getPassword();
						if (password == null) password = "";
						xml = xml.replaceFirst("DEFAULT_NAME",name);
						xml = xml.replaceFirst("DEFAULT_ADDRESS",address);
						xml = xml.replaceFirst("DEFAULT_USERNAME",username);
						xml = xml.replaceFirst("DEFAULT_PASSWORD",password);
						this.oldDataSource = ds;
					}	
				} else {					
					org.osid.provider.Provider provider = (org.osid.provider.Provider)o;
					
					boolean proceed = true;
					edu.tufts.vue.dsm.DataSource ds = null;
					// show dialog containing license, if any
					try {
						if (provider.requestsLicenseAcknowledgement()) {
							String license = provider.getLicense();
							if (license != null) {
								javax.swing.JTextArea area = new javax.swing.JTextArea();
								area.setLineWrap(true);
								area.setText(license);
								area.setEditable(false);
								area.setSize(new Dimension(500,300));
								if (javax.swing.JOptionPane.showOptionDialog(this,
																			 area,
																			 "License Acknowledgement",
																			 javax.swing.JOptionPane.DEFAULT_OPTION,
																			 javax.swing.JOptionPane.QUESTION_MESSAGE,
																			 null,
																			 new Object[] {
																				 "Accept", "Decline"
																			 },
																			 "Decline") != 0) {
									return;
								}
							}
						}
						
						System.out.println("checking if installed");
						if (proceed && (!provider.isInstalled())) { 
							System.out.println("installing...");
							factory = edu.tufts.vue.dsm.impl.VueOsidFactory.getInstance();
							try {
								GUI.activateWaitCursor();
								factory.installProvider(provider.getId());
							} catch (Throwable t1) {
								System.out.println("install failed " + provider.getId().getIdString());
								javax.swing.JOptionPane.showMessageDialog(this,
																		  "Installation Failed",
																		  "OSID Installation Error",
																		  javax.swing.JOptionPane.ERROR_MESSAGE);
								return;
							} finally {
								GUI.clearWaitCursor();
							}
						} else {
							System.out.println("No need to install");
						}
						
						if (proceed) {
							// add to data sources list
							try {
								System.out.println("creating data source");
								ds = new edu.tufts.vue.dsm.impl.VueDataSource(factory.getIdManagerInstance().createId(),
																			  provider.getId(),
																			  true);
							} catch (Throwable t) {
								javax.swing.JOptionPane.showMessageDialog(this,
																		  "Loading Manager Failed",
																		  "OSID Installation Error",
																		  javax.swing.JOptionPane.ERROR_MESSAGE);
								return;
							}
							System.out.println("created data source");
							
							// show configuration, if needed
							if (ds.hasConfiguration()) {
								xml = ds.getConfigurationUIHints();
							} else {
								System.out.println("No configuration to show");
							}
							this.newDataSource = ds;
						}
					} catch (Throwable t) {
						System.out.println("configuration setup failed");
						javax.swing.JOptionPane.showMessageDialog(this,
																  t.getMessage(),
																  "OSID Installation Error",
																  javax.swing.JOptionPane.ERROR_MESSAGE);
						t.printStackTrace();
						return;
					}
				}
				
				if (xml != null) {
					edu.tufts.vue.ui.ConfigurationUI cui = 
					new edu.tufts.vue.ui.ConfigurationUI(new java.io.ByteArrayInputStream(xml.getBytes()));
					cui.setPreferredSize(new Dimension(400,200));
					
					if (javax.swing.JOptionPane.showOptionDialog(this,
																 cui,
																 "Configuration",
																 javax.swing.JOptionPane.DEFAULT_OPTION,
																 javax.swing.JOptionPane.QUESTION_MESSAGE,
																 null,
																 new Object[] {
																	 "Cancel", "Update"
																 },
																 "Update") != 0) {
						if (s != null) {
							if (s.equals(MY_COMPUTER)) {
								java.util.Properties p = cui.getProperties();
								LocalFileDataSource ds = (LocalFileDataSource)this.oldDataSource;
								ds.setDisplayName(p.getProperty("name"));
								ds.setAddress(p.getProperty("address"));
							} else if (s.equals(MY_SAVED_CONTENT)) {
								java.util.Properties p = cui.getProperties();
								FavoritesDataSource ds = (FavoritesDataSource)this.oldDataSource;
								ds.setDisplayName(p.getProperty("name"));
							} else if (s.equals(FTP)) {
								java.util.Properties p = cui.getProperties();
								RemoteFileDataSource ds = (RemoteFileDataSource)this.oldDataSource;
								ds.setDisplayName(p.getProperty("name"));
								ds.setUserName(p.getProperty("username"));
								ds.setPassword(p.getProperty("password"));
								try {
									ds.setAddress(p.getProperty("address")); // this must be set last
								} catch (Exception ex) {
									// ignore any error for now
								}
							}						
						} else {
							try {
								GUI.activateWaitCursor();
								this.newDataSource.setConfiguration(cui.getProperties());
								GUI.invokeAfterAWT(new Runnable() { public void run() {
									try {
										synchronized (dataSourceManager) {
											dataSourceManager.save();
										}
									} catch (Throwable t) {
										System.out.println(t.getMessage());
									}
								}});
								
							} catch (Throwable t2) {
								
							} finally {
								GUI.clearWaitCursor();
							}
						}
					}
				}
				if (this.oldDataSource != null) {
					dataSourceList.addOrdered(this.oldDataSource);
					//					DataSourceViewer.setActiveDataSource(this.oldDataSource);
				} else {
					System.out.println("ready to add new data source to list");
					dataSourceList.addOrdered(this.newDataSource);
					System.out.println("ready to add new data source to manager");
					dataSourceManager.add(this.newDataSource);
				}
				providerListRenderer.setChecked(addLibraryList.getSelectedIndex());
			} catch (Throwable t) {
				t.printStackTrace();
			}
			finally
			{
				providerListRenderer.endWaitingMode();			
	    		addLibraryList.repaint();
				GUI.clearWaitCursor();
				timer.stop();
				this.setCursor(Cursor.getPredefinedCursor(Cursor.DEFAULT_CURSOR));
		}		
	}
	
		public void actionPerformed(ActionEvent ae) {
		
			
		if (ae.getActionCommand().equals("Add")) 
		{		
			this.setCursor(Cursor.getPredefinedCursor(Cursor.WAIT_CURSOR));
			GUI.activateWaitCursor();
			providerListRenderer.invokeWaitingMode();
			repaint();
    	    int ONE_TNTH_SECOND = 100;
    		
    		timer = new Timer(ONE_TNTH_SECOND, new ActionListener() {
    		    public void actionPerformed(ActionEvent evt) {
    		    	repaint();    		    	
    		    }});
    		timer.start();
    		
    		AddDSThread t = new AddDSThread();
    		t.start();
							
		} else 
		{
			providerListRenderer.clearAllChecked();
			setVisible(false);
		}
	}
		private class AddDSThread extends Thread {
		    public AddDSThread() {
		        super();
		    }
		    public void run() {
		    	add();
		    }
		}
}



