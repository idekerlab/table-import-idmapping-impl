package org.cytoscape.tableimport.internal.ui;

import static javax.swing.GroupLayout.DEFAULT_SIZE;
import static javax.swing.GroupLayout.PREFERRED_SIZE;
import static javax.swing.GroupLayout.Alignment.CENTER;
import static org.cytoscape.tableimport.internal.reader.TextDelimiter.BACKSLASH;
import static org.cytoscape.tableimport.internal.reader.TextDelimiter.COLON;
import static org.cytoscape.tableimport.internal.reader.TextDelimiter.COMMA;
import static org.cytoscape.tableimport.internal.reader.TextDelimiter.PIPE;
import static org.cytoscape.tableimport.internal.reader.TextDelimiter.SLASH;
import static org.cytoscape.tableimport.internal.reader.TextDelimiter.SPACE;
import static org.cytoscape.tableimport.internal.reader.TextDelimiter.TAB;
import static org.cytoscape.tableimport.internal.util.AttributeDataType.TYPE_BOOLEAN;
import static org.cytoscape.tableimport.internal.util.AttributeDataType.TYPE_BOOLEAN_LIST;
import static org.cytoscape.tableimport.internal.util.AttributeDataType.TYPE_FLOATING;
import static org.cytoscape.tableimport.internal.util.AttributeDataType.TYPE_FLOATING_LIST;
import static org.cytoscape.tableimport.internal.util.AttributeDataType.TYPE_INTEGER;
import static org.cytoscape.tableimport.internal.util.AttributeDataType.TYPE_INTEGER_LIST;
import static org.cytoscape.tableimport.internal.util.AttributeDataType.TYPE_LONG;
import static org.cytoscape.tableimport.internal.util.AttributeDataType.TYPE_LONG_LIST;
import static org.cytoscape.tableimport.internal.util.AttributeDataType.TYPE_STRING;
import static org.cytoscape.tableimport.internal.util.AttributeDataType.TYPE_STRING_LIST;
import static org.cytoscape.tableimport.internal.util.SourceColumnSemantic.NONE;

import java.awt.Component;
import java.awt.Font;
import java.awt.Window;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Set;

import javax.swing.ButtonGroup;
import javax.swing.ButtonModel;
import javax.swing.DefaultComboBoxModel;
import javax.swing.GroupLayout;
import javax.swing.GroupLayout.ParallelGroup;
import javax.swing.GroupLayout.SequentialGroup;
import javax.swing.JCheckBox;
import javax.swing.JComboBox;
import javax.swing.JComponent;
import javax.swing.JLabel;
import javax.swing.JList;
import javax.swing.JPanel;
import javax.swing.JTable;
import javax.swing.JTextField;
import javax.swing.JToggleButton;
import javax.swing.LayoutStyle.ComponentPlacement;
import javax.swing.ListCellRenderer;
import javax.swing.UIManager;
import javax.swing.event.DocumentEvent;
import javax.swing.event.DocumentListener;
import javax.swing.table.TableColumn;

import org.cytoscape.tableimport.internal.reader.TextDelimiter;
import org.cytoscape.tableimport.internal.ui.PreviewTablePanel.PreviewTableModel;
import org.cytoscape.tableimport.internal.util.AttributeDataType;
import org.cytoscape.tableimport.internal.util.SourceColumnSemantic;
import org.cytoscape.tableimport.internal.util.TypeUtil;
import org.cytoscape.util.swing.IconManager;
import org.cytoscape.util.swing.LookAndFeelUtil;


/*
 * How to use id mapper for table import:
 * go to:
 * path/to/git/table-import-idmapping-impl/table-import-impl
 * % mvn clean install
 * Launch Cytoscape from developer console
 * then, File|Import|Table|File...
 * select a CSV file example with Uniprot identifiers
 * in the preview table, right click on a column header 
 * Id Mapping sub-menu should appear below "List Delimiter" button.
 * 
 * 
 * TODO Id mapper need to be packaged better!
 * TODO Problems with adding column to preview table persists.
 * 
 * Id mapper is here: https://github.com/cytoscape/idmap-impl 
 * 
 * For now, all the needed classes from https://github.com/cytoscape/idmap-impl
 * are in org.cytoscape.tableimport.internal.ui.idmap
 * 
 */
import org.cytoscape.tableimport.internal.ui.idmap.IdMapper;
import org.cytoscape.tableimport.internal.ui.idmap.IdMapping;
import org.cytoscape.tableimport.internal.ui.idmap.KOIdMapper;

@SuppressWarnings("serial")
public class AttributeEditorPanel extends JPanel {
	
	private static final boolean DEBUG = true;

	/*
	 * private final IdMapper id_mapper = new BridgeDbIdMapper(); 
	 */
	private final IdMapper id_mapper = new KOIdMapper(); // could also be a BridgeDbIdMapper()


	private static final String OTHER = "Other:";
	private static final float ICON_FONT_SIZE = 14.0f;


	
	private JTextField attributeNameTextField;
	private final Map<SourceColumnSemantic, JToggleButton> typeButtons = new LinkedHashMap<>();
	private final Map<AttributeDataType, JToggleButton> dataTypeButtons = new LinkedHashMap<>();

	private JToggleButton stringButton;
	private JToggleButton booleanButton;
	private JToggleButton floatingPointButton;
	private JToggleButton integerButton;
	private JToggleButton longButton;
	private JToggleButton stringListButton;
	private JToggleButton booleanListButton;
	private JToggleButton floatingPointListButton;
	private JToggleButton integerListButton;
	private JToggleButton longListButton;

	private JLabel listDelimiterLabel;
	private JComboBox<String> listDelimiterComboBox;
	private JTextField otherTextField;

	/*
	 * Items for id mapping:
	 */
	private JLabel idmapLabelSource;
	private JLabel idmapLabelTarget;
	private JLabel idmapLabelSpecies;
	private JComboBox<String> idmapSourceComboBox;
	private JComboBox<String> idmapTargetComboBox;
	private JComboBox<String> idmapSpeciesComboBox;
	private JCheckBox idmapForceSingle;
	private ArrayList<ArrayList<String>> orig_values_list = null;
	private JLabel idmapTitle;
	
	private ButtonGroup typeButtonGroup;
	private ButtonGroup dataTypeButtonGroup;

	private String attrName;
	private SourceColumnSemantic attrType;
	private final List<SourceColumnSemantic> availableTypes;
	private AttributeDataType attrDataType;
	private String listDelimiter;

	private final IconManager iconManager;

	private JTable previewTable;

	private int colIdx;

	public AttributeEditorPanel(final Window parent, final String attrName,
			final List<SourceColumnSemantic> availableTypes,
			final SourceColumnSemantic attrType,
			final AttributeDataType attrDataType, final String listDelimiter,
			final IconManager iconManager) {
		this.attrName = attrName;
		this.availableTypes = availableTypes;
		this.attrType = attrType;
		this.attrDataType = attrDataType;
		this.listDelimiter = listDelimiter;
		this.iconManager = iconManager;

		if (!availableTypes.contains(NONE))
			availableTypes.add(0, NONE);

		initComponents();
		updateComponents();
	}

	/**
	 * This is the principal method for id mapping.
	 * TODO it still has problems with creating a new preview column!
	 * FIXME it still has problems with creating a new preview column!
	 * 
	 * @param column
	 * @param id_mapper
	 */
	private void mapID(final int column, final IdMapper id_mapper) {
		
		/*
		 * To keep the original values of the preview table.
		 */
		if (orig_values_list == null ) {
			prepareOrginalValiesTable();
		}
		
		/*
		 * Getting source and target for id type. 
		 * Source and target species are the same (for now).
		 * 
		 */
		final String target_type = (String) getIdmapTarget();
		final String source_type = (String) getIdmapSource();
		final String species = (String) getIdmapSpecies();
		
		/*
		 * Name for the new column
		 * TODO should give user ability to make own name, via UI.
		 */
		final String new_name = target_type + "->" + source_type;

		/*
		 * This performs the actual mapping.
		 * Mapping results are stored in map "res".
		 */
        final Map<String, IdMapping> res = id_mapper.map(
				orig_values_list.get(column), source_type, target_type,
				species, species);

        if (DEBUG) {
		    for (int row = 0; row < previewTable.getRowCount(); ++row) {
			    System.out.print(row + " :");
			    System.out.println((String) previewTable.getValueAt(row, column));
		    }
        }

        /*
         * This parses the id mapping result
         */
		if (res != null && !res.isEmpty()) {
			final int orig_row_count = previewTable.getRowCount();

			final String[] new_column = new String[orig_row_count ];
			for (int row = 0; row < orig_row_count; ++row) {
				
				final String source_id = orig_values_list.get(column).get(row);
				final IdMapping id_mapping = res.get(source_id);
				if (id_mapping != null) {
					if (id_mapping.getTargetIds() != null && !id_mapping.getTargetIds().isEmpty()) {
						final StringBuilder ids_str= new StringBuilder();
						final Set<String> target_ids = id_mapping.getTargetIds();
						for (final String target_id : target_ids) {
							ids_str.append( target_id);
							ids_str.append( " ");
						}
						
						new_column[row] = ids_str.toString();
						
					}
					else {
						new_column[row] = "";
					}
				}
				else {
					new_column[row] = "";
				}
				if ( DEBUG) {
                    System.out.println(source_id + "->" + new_column[row]);
				}
			}

			
			/*
			 * From here, the problems start.
			 * It does NOT properly add a new table column...
			 * TODO
			 * FIXME
			 */
			final TableColumn new_tablecolumn = new TableColumn();

			new_tablecolumn.setHeaderValue(new_name);
			previewTable.addColumn(new_tablecolumn);
			
			final PreviewTableModel pt_model = (PreviewTableModel) previewTable
					.getModel();
			
		    for (int row = 0; row < new_column.length; ++row) {
                pt_model.setValueAt(new_column[row], row, 1);
            	// col 1 is just used as test!
            }
		}

	}

	/**
	 * To keep the original values of the preview table.
	 * 
	 */
	private final void prepareOrginalValiesTable() {
		orig_values_list = new ArrayList<ArrayList<String>>();
		for (int col = 0; col < previewTable.getColumnCount(); ++col) {
			final ArrayList<String> n = new ArrayList<String>();
		    orig_values_list.add( n);
		    for (int row = 0; row < previewTable.getRowCount(); ++row) {
		    	//TODO what about list values?
				String v = (String) previewTable.getValueAt(row, col);
				n.add(v);
			}
		}
	}

	
	/**
	 * This returns the id mapper source type (e.g. UniProt) 
	 * 
	 * @return
	 */
	public Object getIdmapSource() {
		return idmapSourceComboBox.getSelectedItem();
	}

	/**
	 * This returns the id mapper target type (e.g. Ensembl) 
	 * 
	 * @return
	 */
	public Object getIdmapTarget() {
		return idmapTargetComboBox.getSelectedItem();
	}

	/**
	 *  This returns the id mapper species  
	 * 
	 * @return
	 */
	public Object getIdmapSpecies() {
		return idmapSpeciesComboBox.getSelectedItem();
	}

	

	/**
	 * This creates a combo box for the id mapping source type.
	 * 
	 * 
	 * @return 
	 */
	private JComboBox<String> getIdmapSourceComboBox() {

		if (idmapSourceComboBox == null) {
			idmapSourceComboBox = new JComboBox<>();
			idmapSourceComboBox.putClientProperty(
					"JComponent.sizeVariant", "small");
			idmapSourceComboBox.setModel(new DefaultComboBoxModel<String>(
					new String[] { KOIdMapper.SYMBOL, KOIdMapper.GENE_ID,
							KOIdMapper.ENSEMBL, KOIdMapper.UniProtKB_AC,
							KOIdMapper.UniProtKB_ID }));

			final ListCellRenderer<? super String> renderer = idmapSourceComboBox
					.getRenderer();

			idmapSourceComboBox
					.setRenderer(new ListCellRenderer<String>() {
						@Override
						public Component getListCellRendererComponent(
								JList<? extends String> list, String value,
								int index, boolean isSelected,
								boolean cellHasFocus) {
							final Component c = renderer
									.getListCellRendererComponent(list, value,
											index, isSelected, cellHasFocus);

							if (OTHER.equals(value) && c instanceof JComponent)
								((JComponent) c).setFont(((JComponent) c)
										.getFont().deriveFont(Font.ITALIC));

							return c;
						}
					});

			idmapSourceComboBox.addActionListener(new ActionListener() {

				@Override
				public void actionPerformed(ActionEvent e) {
					firePropertyChange("IdmapLabelSource", listDelimiter,
							listDelimiter = getListDelimiter());
					
					/*
					 * Calling the mapping function 
					 */
					mapID(colIdx, id_mapper);

				}

			});
		}

		return idmapSourceComboBox;
	}

	/**
	 * This creates a combo box for the id mapping target type.
	 * 
	 * 
	 * @return 
	 */
	private JComboBox<String> getIdmapTargetComboBox() {
		if (idmapTargetComboBox == null) {
			idmapTargetComboBox = new JComboBox<>();
			idmapTargetComboBox.putClientProperty(
					"JComponent.sizeVariant", "small");
			idmapTargetComboBox
					.setModel(new DefaultComboBoxModel<String>(
							new String[] { KOIdMapper.SYMBOL,
									KOIdMapper.GENE_ID, KOIdMapper.ENSEMBL,
									KOIdMapper.SYNONYMS,
									KOIdMapper.UniProtKB_AC,
									KOIdMapper.UniProtKB_ID, KOIdMapper.RefSeq,
									KOIdMapper.GI, KOIdMapper.PDB,
									KOIdMapper.GO, KOIdMapper.UniRef100,
									KOIdMapper.UniRef90, KOIdMapper.UniRef50,
									KOIdMapper.UniParc, KOIdMapper.PIR,
									KOIdMapper.EMBL }));

			final ListCellRenderer<? super String> renderer = idmapTargetComboBox
					.getRenderer();

			idmapTargetComboBox
					.setRenderer(new ListCellRenderer<String>() {
						@Override
						public Component getListCellRendererComponent(
								JList<? extends String> list, String value,
								int index, boolean isSelected,
								boolean cellHasFocus) {
							final Component c = renderer
									.getListCellRendererComponent(list, value,
											index, isSelected, cellHasFocus);

							if (OTHER.equals(value) && c instanceof JComponent)
								((JComponent) c).setFont(((JComponent) c)
										.getFont().deriveFont(Font.ITALIC));

							return c;
						}
					});

			idmapTargetComboBox.addActionListener(new ActionListener() {
				@Override
				public void actionPerformed(ActionEvent e) {

					firePropertyChange("IdmapLabelTarget", listDelimiter,
							listDelimiter = getListDelimiter());
					
					/*
					 * Calling the mapping function 
					 */
					mapID(colIdx, id_mapper);
					
				}
			});
		}

		return idmapTargetComboBox;
	}

	/**
	 * This creates a combo box for the id mapping species.
	 * 
	 * 
	 * @return 
	 */
	private JComboBox<String> getIdmapSpeciesComboBox() {
		if (idmapSpeciesComboBox == null) {
			idmapSpeciesComboBox = new JComboBox<>();
			idmapSpeciesComboBox.putClientProperty(
					"JComponent.sizeVariant", "small");
			idmapSpeciesComboBox
					.setModel(new DefaultComboBoxModel<String>(new String[] {
							KOIdMapper.HUMAN, KOIdMapper.MOUSE, KOIdMapper.FLY,
							KOIdMapper.YEAST }));

			final ListCellRenderer<? super String> renderer = idmapSpeciesComboBox
					.getRenderer();

			idmapSpeciesComboBox
					.setRenderer(new ListCellRenderer<String>() {
						@Override
						public Component getListCellRendererComponent(
								JList<? extends String> list, String value,
								int index, boolean isSelected,
								boolean cellHasFocus) {
							final Component c = renderer
									.getListCellRendererComponent(list, value,
											index, isSelected, cellHasFocus);

							if (OTHER.equals(value) && c instanceof JComponent)
								((JComponent) c).setFont(((JComponent) c)
										.getFont().deriveFont(Font.ITALIC));

							return c;
						}
					});

			idmapSpeciesComboBox.addActionListener(new ActionListener() {
				@Override
				public void actionPerformed(ActionEvent e) {

					firePropertyChange("IdmapLabelSpecies", listDelimiter,
							listDelimiter = getListDelimiter());
			
					/*
					 * Calling the mapping function 
					 */
					mapID(colIdx, id_mapper);

				}
			});
		}

		return idmapSpeciesComboBox;
	}

	

	private void initComponents() {
		listDelimiterLabel = new JLabel("List Delimiter:");
		listDelimiterLabel.putClientProperty("JComponent.sizeVariant", "small");
		
		/*
		 * Id mapping related items:
		 */
		idmapTitle = new JLabel("Id Mapping:"); 
		idmapForceSingle = new JCheckBox("Force single");
		idmapLabelSource = new JLabel("Source:"); 
		idmapLabelTarget = new JLabel("Target:");
		idmapLabelSpecies = new JLabel("Species:");
		idmapLabelSource.putClientProperty("JComponent.sizeVariant", "small");
		idmapLabelTarget.putClientProperty("JComponent.sizeVariant", "small");
		idmapLabelSpecies.putClientProperty("JComponent.sizeVariant", "small");
		

		typeButtonGroup = new ButtonGroup();
		dataTypeButtonGroup = new ButtonGroup();

		final List<JToggleButton> dataTypeBtnList = new ArrayList<>();

		dataTypeBtnList.add(stringButton = createDataTypeButton(TYPE_STRING));
		dataTypeBtnList.add(integerButton = createDataTypeButton(TYPE_INTEGER));
		dataTypeBtnList.add(longButton = createDataTypeButton(TYPE_LONG));
		dataTypeBtnList
				.add(floatingPointButton = createDataTypeButton(TYPE_FLOATING));
		dataTypeBtnList.add(booleanButton = createDataTypeButton(TYPE_BOOLEAN));
		dataTypeBtnList
				.add(stringListButton = createDataTypeButton(TYPE_STRING_LIST));
		dataTypeBtnList
				.add(integerListButton = createDataTypeButton(TYPE_INTEGER_LIST));
		dataTypeBtnList
				.add(longListButton = createDataTypeButton(TYPE_LONG_LIST));
		dataTypeBtnList
				.add(floatingPointListButton = createDataTypeButton(TYPE_FLOATING_LIST));
		dataTypeBtnList
				.add(booleanListButton = createDataTypeButton(TYPE_BOOLEAN_LIST));

		setStyles(dataTypeBtnList);

		final GroupLayout layout = new GroupLayout(this);
		this.setLayout(layout);
		layout.setAutoCreateContainerGaps(true);
		layout.setAutoCreateGaps(false);

		final SequentialGroup typeHGroup = layout.createSequentialGroup();
		final ParallelGroup typeVGroup = layout.createParallelGroup(CENTER,
				false);

		for (SourceColumnSemantic type : availableTypes) {
			final JToggleButton btn = createTypeButton(type);
			typeHGroup.addComponent(btn, DEFAULT_SIZE, DEFAULT_SIZE,
					Short.MAX_VALUE);
			typeVGroup.addComponent(btn);
		}

		setStyles(new ArrayList<JToggleButton>(typeButtons.values()));

		final JLabel typeLabel = new JLabel("Meaning:");
		typeLabel.putClientProperty("JComponent.sizeVariant", "small");

		final JLabel dataTypeLabel = new JLabel("Data Type:");
		dataTypeLabel.putClientProperty("JComponent.sizeVariant", "small");

		layout.setHorizontalGroup(layout
				.createParallelGroup(CENTER, true)
				.addComponent(getAttributeNameTextField(), DEFAULT_SIZE,
						DEFAULT_SIZE, Short.MAX_VALUE)
				.addComponent(typeLabel, DEFAULT_SIZE, DEFAULT_SIZE,
						Short.MAX_VALUE)
				.addGroup(typeHGroup)
				.addComponent(dataTypeLabel, DEFAULT_SIZE, DEFAULT_SIZE,
						Short.MAX_VALUE)
				.addGroup(
						layout.createSequentialGroup()
								.addComponent(stringButton, DEFAULT_SIZE,
										DEFAULT_SIZE, Short.MAX_VALUE)
								.addComponent(integerButton, DEFAULT_SIZE,
										DEFAULT_SIZE, Short.MAX_VALUE)
								.addComponent(longButton, DEFAULT_SIZE,
										DEFAULT_SIZE, Short.MAX_VALUE)
								.addComponent(floatingPointButton,
										DEFAULT_SIZE, DEFAULT_SIZE,
										Short.MAX_VALUE)
								.addComponent(booleanButton, DEFAULT_SIZE,
										DEFAULT_SIZE, Short.MAX_VALUE))
				.addGroup(
						layout.createSequentialGroup()
								.addComponent(stringListButton, DEFAULT_SIZE,
										DEFAULT_SIZE, Short.MAX_VALUE)
								.addComponent(integerListButton, DEFAULT_SIZE,
										DEFAULT_SIZE, Short.MAX_VALUE)
								.addComponent(longListButton, DEFAULT_SIZE,
										DEFAULT_SIZE, Short.MAX_VALUE)
								.addComponent(floatingPointListButton,
										DEFAULT_SIZE, DEFAULT_SIZE,
										Short.MAX_VALUE)
								.addComponent(booleanListButton, DEFAULT_SIZE,
										DEFAULT_SIZE, Short.MAX_VALUE))
				.addGroup(
						layout.createSequentialGroup()
								.addComponent(listDelimiterLabel)
								.addPreferredGap(ComponentPlacement.RELATED)
								.addComponent(getListDelimiterComboBox(),
										PREFERRED_SIZE, DEFAULT_SIZE,
										PREFERRED_SIZE)
								.addComponent(getOtherTextField(), 12, 36,
										Short.MAX_VALUE))

				.addGroup(
						layout.createSequentialGroup().addComponent(idmapTitle))

				.addGroup(
						layout.createSequentialGroup()
								.addComponent(idmapLabelSource)
								.addPreferredGap(ComponentPlacement.RELATED)
								.addComponent(getIdmapSourceComboBox(),
										PREFERRED_SIZE, DEFAULT_SIZE,
										PREFERRED_SIZE)
								.addComponent(getOtherTextField(), 12, 36,
										Short.MAX_VALUE))
				.addGroup(
						layout.createSequentialGroup()
								.addComponent(idmapLabelTarget)
								.addPreferredGap(ComponentPlacement.RELATED)
								.addComponent(getIdmapTargetComboBox(),
										PREFERRED_SIZE, DEFAULT_SIZE,
										PREFERRED_SIZE)
								.addComponent(getOtherTextField(), 12, 36,
										Short.MAX_VALUE))
				.addGroup(
						layout.createSequentialGroup()
								.addComponent(idmapLabelSpecies)
								.addPreferredGap(ComponentPlacement.RELATED)
								.addComponent(getIdmapSpeciesComboBox(),
										PREFERRED_SIZE, DEFAULT_SIZE,
										PREFERRED_SIZE)
								.addComponent(getOtherTextField(), 12, 36,
										Short.MAX_VALUE))

				.addGroup(
						layout.createSequentialGroup().addComponent(
								idmapForceSingle))

		);
		layout.setVerticalGroup(layout
				.createSequentialGroup()
				.addComponent(getAttributeNameTextField(), PREFERRED_SIZE,
						DEFAULT_SIZE, PREFERRED_SIZE)
				.addPreferredGap(ComponentPlacement.UNRELATED)
				.addComponent(typeLabel, PREFERRED_SIZE, DEFAULT_SIZE,
						PREFERRED_SIZE)
				.addPreferredGap(ComponentPlacement.RELATED)
				.addGroup(typeVGroup)
				.addPreferredGap(ComponentPlacement.UNRELATED)
				.addComponent(dataTypeLabel, PREFERRED_SIZE, DEFAULT_SIZE,
						PREFERRED_SIZE)
				.addPreferredGap(ComponentPlacement.RELATED)
				.addGroup(
						layout.createParallelGroup(CENTER)
								.addComponent(stringButton)
								.addComponent(integerButton)
								.addComponent(longButton)
								.addComponent(floatingPointButton)
								.addComponent(booleanButton))
				.addGroup(
						layout.createParallelGroup(CENTER)
								.addComponent(stringListButton)
								.addComponent(integerListButton)
								.addComponent(longListButton)
								.addComponent(floatingPointListButton)
								.addComponent(booleanListButton))
				.addGroup(
						layout.createSequentialGroup()
								.addPreferredGap(ComponentPlacement.RELATED)
								.addGroup(
										layout.createParallelGroup(CENTER)
												.addComponent(
														listDelimiterLabel)
												.addComponent(
														getListDelimiterComboBox(),
														PREFERRED_SIZE,
														DEFAULT_SIZE,
														PREFERRED_SIZE)
												.addComponent(
														getOtherTextField(),
														PREFERRED_SIZE,
														DEFAULT_SIZE,
														PREFERRED_SIZE)))

				.addGroup(
						layout.createSequentialGroup()
								.addPreferredGap(ComponentPlacement.RELATED)
								.addGroup(
										layout.createParallelGroup(CENTER)
												.addComponent(idmapTitle)))

				.addGroup(
						layout.createSequentialGroup()
								.addPreferredGap(ComponentPlacement.RELATED)
								.addGroup(
										layout.createParallelGroup(CENTER)
												.addComponent(idmapLabelSource)
												.addComponent(
														getIdmapSourceComboBox(),
														PREFERRED_SIZE,
														DEFAULT_SIZE,
														PREFERRED_SIZE)
												.addComponent(
														getOtherTextField(),
														PREFERRED_SIZE,
														DEFAULT_SIZE,
														PREFERRED_SIZE)))
				.addGroup(
						layout.createSequentialGroup()
								.addPreferredGap(ComponentPlacement.RELATED)
								.addGroup(
										layout.createParallelGroup(CENTER)
												.addComponent(idmapLabelTarget)
												.addComponent(
														getIdmapTargetComboBox(),
														PREFERRED_SIZE,
														DEFAULT_SIZE,
														PREFERRED_SIZE)
												.addComponent(
														getOtherTextField(),
														PREFERRED_SIZE,
														DEFAULT_SIZE,
														PREFERRED_SIZE)))
				.addGroup(
						layout.createSequentialGroup()
								.addPreferredGap(ComponentPlacement.RELATED)
								.addGroup(
										layout.createParallelGroup(CENTER)
												.addComponent(idmapLabelSpecies)
												.addComponent(
														getIdmapSpeciesComboBox(),
														PREFERRED_SIZE,
														DEFAULT_SIZE,
														PREFERRED_SIZE)
												.addComponent(
														getOtherTextField(),
														PREFERRED_SIZE,
														DEFAULT_SIZE,
														PREFERRED_SIZE))

								.addGroup(
										layout.createSequentialGroup()
												.addPreferredGap(
														ComponentPlacement.RELATED)
												.addGroup(
														layout.createParallelGroup(
																CENTER)
																.addComponent(
																		idmapForceSingle)))

				));
	}

	protected JTextField getAttributeNameTextField() {
		if (attributeNameTextField == null) {
			attributeNameTextField = new JTextField(attrName);
			attributeNameTextField.setToolTipText("Column Name");
			attributeNameTextField.putClientProperty("JComponent.sizeVariant",
					"small");
			attributeNameTextField.getDocument().addDocumentListener(
					new DocumentListener() {
						@Override
						public void changedUpdate(DocumentEvent e) {
							onTextChanged();
						}

						@Override
						public void removeUpdate(DocumentEvent e) {
							onTextChanged();
						}

						@Override
						public void insertUpdate(DocumentEvent e) {
							onTextChanged();
						}

						public void onTextChanged() {
							firePropertyChange("attributeName", attrName,
									attrName = getAttributeName());
						}
					});
		}

		return attributeNameTextField;
	}

	private JComboBox<String> getListDelimiterComboBox() {
		if (listDelimiterComboBox == null) {
			listDelimiterComboBox = new JComboBox<>();
			listDelimiterComboBox.putClientProperty("JComponent.sizeVariant",
					"small");
			listDelimiterComboBox.setModel(new DefaultComboBoxModel<String>(
					new String[] { PIPE.toString(), COLON.toString(),
							SLASH.toString(), BACKSLASH.toString(),
							COMMA.toString(), SPACE.toString(), TAB.toString(),
							OTHER }));

			final ListCellRenderer<? super String> renderer = listDelimiterComboBox
					.getRenderer();

			listDelimiterComboBox.setRenderer(new ListCellRenderer<String>() {
				@Override
				public Component getListCellRendererComponent(
						JList<? extends String> list, String value, int index,
						boolean isSelected, boolean cellHasFocus) {
					final Component c = renderer.getListCellRendererComponent(
							list, value, index, isSelected, cellHasFocus);

					if (OTHER.equals(value) && c instanceof JComponent)
						((JComponent) c).setFont(((JComponent) c).getFont()
								.deriveFont(Font.ITALIC));

					return c;
				}
			});

			listDelimiterComboBox.addActionListener(new ActionListener() {
				@Override
				public void actionPerformed(ActionEvent e) {
					final boolean isOther = isOtherDelimiterSelected();
					getOtherTextField().setEnabled(isOther);

					if (!isOther || !getOtherTextField().getText().isEmpty())
						firePropertyChange("listDelimiter", listDelimiter,
								listDelimiter = getListDelimiter());
				}
			});
		}

		return listDelimiterComboBox;
	}
	

	public String getAttributeName() {
		return getAttributeNameTextField().getText().trim();
	}

	public SourceColumnSemantic getAttributeType() {
		final ButtonModel model = typeButtonGroup.getSelection();

		for (Entry<SourceColumnSemantic, JToggleButton> entry : typeButtons
				.entrySet()) {
			final JToggleButton btn = entry.getValue();

			if (btn.getModel().equals(model))
				return entry.getKey();
		}

		return NONE;
	}

	public AttributeDataType getAttributeDataType() {
		final ButtonModel model = dataTypeButtonGroup.getSelection();

		for (Entry<AttributeDataType, JToggleButton> entry : dataTypeButtons
				.entrySet()) {
			final JToggleButton btn = entry.getValue();

			if (btn.getModel().equals(model))
				return entry.getKey();
		}

		return TYPE_STRING;
	}

	public String getListDelimiter() {
		if (isOtherDelimiterSelected())
			return getOtherTextField().getText();

		final String label = getListDelimiterComboBox().getSelectedItem()
				.toString();
		final TextDelimiter del = TextDelimiter.getByLabel(label);

		return del != null ? del.getDelimiter() : null;
	}

	private JTextField getOtherTextField() {
		if (otherTextField == null) {
			otherTextField = new JTextField();
			otherTextField.putClientProperty("JComponent.sizeVariant", "small");
			otherTextField.getDocument().addDocumentListener(
					new DocumentListener() {
						@Override
						public void changedUpdate(DocumentEvent e) {
							onTextChanged();
						}

						@Override
						public void removeUpdate(DocumentEvent e) {
							onTextChanged();
						}

						@Override
						public void insertUpdate(DocumentEvent e) {
							onTextChanged();
						}

						public void onTextChanged() {
							firePropertyChange("listDelimiter", listDelimiter,
									listDelimiter = getListDelimiter());
						}
					});
		}

		return otherTextField;
	}

	private void setStyles(final List<JToggleButton> btnList) {
		if (LookAndFeelUtil.isAquaLAF()) {
			for (int i = 0; i < btnList.size(); i++) {
				final JToggleButton btn = btnList.get(i);
				btn.putClientProperty("JButton.buttonType", "segmentedGradient");
				btn.putClientProperty("JButton.segmentPosition", "only");
				btn.putClientProperty("JComponent.sizeVariant", "small");
			}
		}

		LookAndFeelUtil.equalizeSize(btnList.toArray(new JToggleButton[btnList
				.size()]));
	}

	private void updateComponents() {
		updateTypeButtonGroup();
		updateDataTypeButtonGroup();
		updateListDelimiterComboBox();
		updateOtherTextField();
		updateTypeButtons();
		updateDataTypeButtons();
	}

	private void updateTypeButtons() {
		final AttributeDataType dataType = getAttributeDataType();

		for (Entry<SourceColumnSemantic, JToggleButton> entry : typeButtons
				.entrySet()) {
			final SourceColumnSemantic type = entry.getKey();
			final JToggleButton btn = entry.getValue();
			btn.setEnabled(TypeUtil.isValid(type, dataType));
			btn.setForeground(btn.isEnabled() ? type.getForeground()
					: UIManager.getColor("Button.disabledForeground"));
		}
	}

	private void updateDataTypeButtons() {
		final SourceColumnSemantic type = getAttributeType();

		for (Entry<AttributeDataType, JToggleButton> entry : dataTypeButtons
				.entrySet()) {
			final AttributeDataType dataType = entry.getKey();
			final JToggleButton btn = entry.getValue();
			btn.setEnabled(TypeUtil.isValid(type, dataType));
		}
	}

	private void updateTypeButtonGroup() {
		JToggleButton btn = typeButtons.get(attrType);

		if (btn == null)
			btn = typeButtons.get(NONE);
		if (btn != null)
			typeButtonGroup.setSelected(btn.getModel(), true);
	}

	private void updateDataTypeButtonGroup() {
		final JToggleButton button = dataTypeButtons.get(attrDataType);
		final ButtonModel model = button != null ? button.getModel() : null;

		if (model != null)
			dataTypeButtonGroup.setSelected(model, true);
	}

	private void updateListDelimiterComboBox() {
		listDelimiterLabel.setEnabled(attrDataType.isList());
		getListDelimiterComboBox().setEnabled(attrDataType.isList());

		if (listDelimiter == null || listDelimiter.isEmpty()) {
			getListDelimiterComboBox().setSelectedIndex(0);
		} else {
			for (int i = 0; i < getListDelimiterComboBox().getItemCount(); i++) {
				final String label = getListDelimiterComboBox().getItemAt(i);
				final TextDelimiter del = TextDelimiter.getByLabel(label);

				if (del != null && listDelimiter.equals(del.getDelimiter())) {
					getListDelimiterComboBox().setSelectedIndex(i);

					return;
				}
			}

			getListDelimiterComboBox().setSelectedItem(OTHER);
		}
	}

	private void updateOtherTextField() {
		getOtherTextField().setEnabled(
				attrDataType.isList() && isOtherDelimiterSelected());

		if (listDelimiter != null && !listDelimiter.isEmpty()
				&& isOtherDelimiterSelected())
			getOtherTextField().setText(listDelimiter);
	}

	private boolean isOtherDelimiterSelected() {
		return OTHER.equals(getListDelimiterComboBox().getSelectedItem()
				.toString());
	}

	private JToggleButton createTypeButton(final SourceColumnSemantic type) {
		final JToggleButton btn = new JToggleButton(type.getText());
		btn.setToolTipText(type.getDescription());
		btn.setFont(iconManager.getIconFont(ICON_FONT_SIZE));
		btn.setForeground(type.getForeground());
		btn.setName(type.toString());
		btn.addActionListener(new ActionListener() {
			@Override
			public void actionPerformed(ActionEvent e) {
				updateDataTypeButtons();
				firePropertyChange("attributeType", attrType,
						attrType = getAttributeType());
			}
		});

		typeButtonGroup.add(btn);
		typeButtons.put(type, btn);

		return btn;
	}

	private JToggleButton createDataTypeButton(final AttributeDataType dataType) {
		final JToggleButton btn = new JToggleButton(dataType.getText());
		btn.setToolTipText(dataType.getDescription());
		btn.setFont(new Font("Serif", Font.BOLD, 11)); // This font is used as
														// an icon--Don't change
														// it!
		btn.setName(dataType.toString());
		btn.addActionListener(new DataTypeButtonActionListener(dataType
				.isList()));

		dataTypeButtonGroup.add(btn);
		dataTypeButtons.put(dataType, btn);

		return btn;
	}

	private class DataTypeButtonActionListener implements ActionListener {

		final boolean isList;

		DataTypeButtonActionListener(final boolean isList) {
			this.isList = isList;
		}

		@Override
		public void actionPerformed(ActionEvent e) {
			listDelimiterLabel.setEnabled(isList);
			getListDelimiterComboBox().setEnabled(isList);
			getOtherTextField()
					.setEnabled(isList && isOtherDelimiterSelected());
			updateTypeButtons();
			firePropertyChange("attributeDataType", attrDataType,
					attrDataType = getAttributeDataType());
		}
	}

	public void setTable(JTable previewTable) {
		this.previewTable = previewTable;

	}


	public void setColIdx(int colIdx) {
		this.colIdx = colIdx;

	}
	
}
