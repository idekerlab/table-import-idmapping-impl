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


// TODO Id mapper need to be packaged better!
// Id mapper is here: https://github.com/cytoscape/idmap-impl
import org.cytoscape.tableimport.internal.ui.idmap.IdMapper;
import org.cytoscape.tableimport.internal.ui.idmap.IdMapping;
import org.cytoscape.tableimport.internal.ui.idmap.KOIdMapper;

@SuppressWarnings("serial")
public class AttributeEditorPanel extends JPanel {

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

	private JLabel idmapLabelSource;
	private JLabel idmapLabelTarget;
	private JLabel idmapLabelSpecies;

	private JComboBox<String> idmapLabelSourceComboBox;
	private JComboBox<String> idmapLabelTargetComboBox;
	private JComboBox<String> idmapLabelSpeciesComboBox;

	private JCheckBox idmapForceSingle;

	private ButtonGroup typeButtonGroup;
	private ButtonGroup dataTypeButtonGroup;

	private String attrName;
	private SourceColumnSemantic attrType;
	private final List<SourceColumnSemantic> availableTypes;
	private AttributeDataType attrDataType;
	private String listDelimiter;

	private final IconManager iconManager;

	private JTable previewTable;

	private ArrayList<ArrayList<String>> orig_values_list = new ArrayList<ArrayList<String>>();

	private JLabel idmapTitle;

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

	public Object getIdmapLabelSource() {
		return idmapLabelSourceComboBox.getSelectedItem();
	}

	public Object getIdmapLabelTarget() {
		return idmapLabelTargetComboBox.getSelectedItem();
	}

	public Object getIdmapLabelSpecies() {
		return idmapLabelSpeciesComboBox.getSelectedItem();
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

	private void initComponents() {
		listDelimiterLabel = new JLabel("List Delimiter:");
		listDelimiterLabel.putClientProperty("JComponent.sizeVariant", "small");
		idmapTitle = new JLabel("Id Mapping:");
		idmapForceSingle = new JCheckBox("Force single");
		idmapLabelSource = new JLabel("Source:"); // CZ
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
								.addComponent(getIdmapLabelSourceComboBox(),
										PREFERRED_SIZE, DEFAULT_SIZE,
										PREFERRED_SIZE)
								.addComponent(getOtherTextField(), 12, 36,
										Short.MAX_VALUE))
				.addGroup(
						layout.createSequentialGroup()
								.addComponent(idmapLabelTarget)
								.addPreferredGap(ComponentPlacement.RELATED)
								.addComponent(getIdmapLabelTargetComboBox(),
										PREFERRED_SIZE, DEFAULT_SIZE,
										PREFERRED_SIZE)
								.addComponent(getOtherTextField(), 12, 36,
										Short.MAX_VALUE))
				.addGroup(
						layout.createSequentialGroup()
								.addComponent(idmapLabelSpecies)
								.addPreferredGap(ComponentPlacement.RELATED)
								.addComponent(getIdmapLabelSpeciesComboBox(),
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
														getIdmapLabelSourceComboBox(),
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
														getIdmapLabelTargetComboBox(),
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
														getIdmapLabelSpeciesComboBox(),
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

	private JComboBox<String> getIdmapLabelSourceComboBox() {
		if (idmapLabelSourceComboBox == null) {
			idmapLabelSourceComboBox = new JComboBox<>();
			idmapLabelSourceComboBox.putClientProperty(
					"JComponent.sizeVariant", "small");
			idmapLabelSourceComboBox.setModel(new DefaultComboBoxModel<String>(
					new String[] { KOIdMapper.SYMBOL, KOIdMapper.GENE_ID, KOIdMapper.ENSEMBL, KOIdMapper.UniProtKB_AC,
							KOIdMapper.UniProtKB_ID }));

			final ListCellRenderer<? super String> renderer = idmapLabelSourceComboBox
					.getRenderer();

			idmapLabelSourceComboBox
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

			idmapLabelSourceComboBox.addActionListener(new ActionListener() {

				@Override
				public void actionPerformed(ActionEvent e) {
					firePropertyChange("IdmapLabelSource", listDelimiter,
							listDelimiter = getListDelimiter());
					System.out.println(getIdmapLabelSource());
					IdMapper id_mapper = new KOIdMapper();
					mapID(colIdx,id_mapper);

				}

			});
		}

		return idmapLabelSourceComboBox;
	}

	private void mapID(final int column, final IdMapper id_mapper) {
		if (orig_values_list.isEmpty()) {
			orig_values_list.add(null);
			orig_values_list.add(null);
		}

		if (orig_values_list.get(column) == null) {
			orig_values_list.set(column, new ArrayList<String>());
			for (int row = 0; row < previewTable.getRowCount(); ++row) {
				String array_element = (String) previewTable.getValueAt(row,
						column);
				orig_values_list.get(column).add(array_element);
			}
		}

		
		Map<String, IdMapping> res = id_mapper.map(orig_values_list.get(column),
                  "source_type",
               "target_type",
               "source_species",
                "target_species");
		
		
		//SortedMap<String, SortedSet<String>> res = run(orig_values_list
		//		.get(column));

		for (int row = 0; row < previewTable.getRowCount(); ++row) {
			System.out.print(row + " :");
			System.out.println((String) previewTable.getValueAt(row, column));
		}

		if (res != null && !res.isEmpty()) {
			final int orig_col_count = previewTable.getColumnCount();
			System.out.println("orig col count " + orig_col_count);
			final int orig_row_count = previewTable.getRowCount();
			System.out.println("orig row count " + orig_row_count);
			

			final String[] new_column = new String[orig_row_count + 0];
			for (int row = 0; row < orig_row_count; ++row) {
				// SortedSet<String> r =
				// res.get(orig_values_list.get(column).get(
				// row));
				IdMapping r = res.get(previewTable.getValueAt(row,
						column));
				if (r.getTargetIds() != null && !r.getTargetIds().isEmpty()) {
					new_column[row] = r.getTargetIds().iterator().next();
					// previewTable.setValueAt(r.first(), row, orig_col_count);
				} else {
					new_column[row] = "_";
					// previewTable.setValueAt(
					// orig_values_list.get(column).get(row), row, column);
				}

			}

			// TableColumn c = new TableColumn();

			// c.setHeaderValue("a");
			// System.out.println(previewTable.getModel());
			PreviewTableModel model = (PreviewTableModel) previewTable
					.getModel();
			
			// model.setColumnCount(orig_col_count + 1);

			System.out.println("orig model col count " + model.getColumnCount());
			System.out.println("orig model row count " + model.getRowCount());

			
			model.addColumn("mapped", new_column);
			
			
			TableColumn c = new TableColumn();

			c.setHeaderValue("a");
			
			previewTable.addColumn(c);
			// model.addColumn("b", news);
			
			
			System.out.println("new model col count " + model.getColumnCount());
			System.out.println("new model row count " + model.getRowCount());


			// previewTable.addColumn(c);
			final int new_col_count = previewTable.getColumnCount();
			System.out.println("new col count " + new_col_count);
			final int new_row_count = previewTable.getRowCount();
			System.out.println("new row count " + new_row_count);

		}

	}

//	private void mapID_old(final int column) {
//		if (orig_values_list.isEmpty()) {
//			orig_values_list.add(null);
//			orig_values_list.add(null);
//		}
//
//		if (orig_values_list.get(column) == null) {
//			orig_values_list.set(column, new ArrayList<String>());
//			for (int row = 0; row < previewTable.getRowCount(); ++row) {
//				String array_element = (String) previewTable.getValueAt(row,
//						column);
//				orig_values_list.get(column).add(array_element);
//			}
//		}
//
//		SortedMap<String, SortedSet<String>> res = run(orig_values_list
//				.get(column));
//
//		if (res != null && !res.isEmpty()) {
//			final int orig_col_count = previewTable.getColumnCount();
//			System.out.println("orig_col_count " + orig_col_count);
//			TableColumn c = new TableColumn();
//
//			c.setHeaderValue("a");
//
//			previewTable.getColumnModel().addColumn(c);
//			System.out.println("new  col count "
//					+ previewTable.getColumnCount());
//
//			previewTable.addColumn(c);
//			for (int row = 0; row < previewTable.getRowCount(); ++row) {
//				SortedSet<String> r = res.get(orig_values_list.get(column).get(
//						row));
//				if (r != null && !r.isEmpty()) {
//					previewTable.setValueAt(r.first(), row, orig_col_count);
//				} else {
//					previewTable.setValueAt(
//							orig_values_list.get(column).get(row), row, column);
//				}
//
//			}
//		}
//
//	}

	private JComboBox<String> getIdmapLabelTargetComboBox() {
		if (idmapLabelTargetComboBox == null) {
			idmapLabelTargetComboBox = new JComboBox<>();
			idmapLabelTargetComboBox.putClientProperty(
					"JComponent.sizeVariant", "small");
			idmapLabelTargetComboBox
					.setModel(new DefaultComboBoxModel<String>(new String[] {
							KOIdMapper.SYMBOL, KOIdMapper.GENE_ID, KOIdMapper.ENSEMBL, KOIdMapper.SYNONYMS, KOIdMapper.UniProtKB_AC,
							KOIdMapper.UniProtKB_ID, KOIdMapper.RefSeq, KOIdMapper.GI, KOIdMapper.PDB, KOIdMapper.GO, KOIdMapper.UniRef100,
							KOIdMapper.UniRef90, KOIdMapper.UniRef50, KOIdMapper.UniParc, KOIdMapper.PIR, KOIdMapper.EMBL }));

			final ListCellRenderer<? super String> renderer = idmapLabelTargetComboBox
					.getRenderer();

			idmapLabelTargetComboBox
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

			idmapLabelTargetComboBox.addActionListener(new ActionListener() {
				@Override
				public void actionPerformed(ActionEvent e) {

					firePropertyChange("IdmapLabelTarget", listDelimiter,
							listDelimiter = getListDelimiter());
					System.out.println(getIdmapLabelTarget());
					IdMapper id_mapper = new KOIdMapper();
					mapID(colIdx,id_mapper);
					;
				}
			});
		}

		return idmapLabelTargetComboBox;
	}

	private JComboBox<String> getIdmapLabelSpeciesComboBox() {
		if (idmapLabelSpeciesComboBox == null) {
			idmapLabelSpeciesComboBox = new JComboBox<>();
			idmapLabelSpeciesComboBox.putClientProperty(
					"JComponent.sizeVariant", "small");
			idmapLabelSpeciesComboBox
					.setModel(new DefaultComboBoxModel<String>(new String[] {
							KOIdMapper.HUMAN, KOIdMapper.MOUSE, KOIdMapper.FLY, KOIdMapper.YEAST }));

			final ListCellRenderer<? super String> renderer = idmapLabelSpeciesComboBox
					.getRenderer();

			idmapLabelSpeciesComboBox
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

			idmapLabelSpeciesComboBox.addActionListener(new ActionListener() {
				@Override
				public void actionPerformed(ActionEvent e) {

					firePropertyChange("IdmapLabelSpecies", listDelimiter,
							listDelimiter = getListDelimiter());

					System.out.println(getIdmapLabelSpecies());
					IdMapper id_mapper = new KOIdMapper();
					mapID(colIdx,id_mapper);
					
				}
			});
		}

		return idmapLabelSpeciesComboBox;
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

	// -----------------

	// https://github.com/cytoscape-ci/service-idmapping
//	public static final String UniProtKB_AC = "UniProtKB-AC";
//	public static final String UniProtKB_ID = "UniProtKB-ID";
//	public static final String RefSeq = "RefSeq";
//	public static final String GI = "GI";
//	public static final String PDB = "PDB";
//	public static final String GO = "GO";
//	public static final String UniRef100 = "UniRef100";
//	public static final String UniRef90 = "UniRef90";
//	public static final String UniRef50 = "UniRef50";
//	public static final String UniParc = "UniParc";
//	public static final String PIR = "PIR";
//	public static final String EMBL = "EMBL";
//	public static final String GENE_ID = "GeneID";
//	public static final String ENSEMBL = "Ensembl";
//	public static final String SYMBOL = "Symbol";
//	public static final String SYNONYMS = "Synonyms";
//	public static final String DEFAULT_MAP_SERVICE_URL_STR = "http://ci-dev-serv.ucsd.edu:3000/map";
//	public static final String HUMAN = "human";
//	public static final String MOUSE = "mouse";
//	public static final String FLY = "fly";
//	public static final String YEAST = "yeast";
//
//	private static final String UNMATCHED = "unmatched";
//	private static final String MATCHED = "matched";
//	private static final String MATCHES = "matches";
//	private static final String IN = "in";
//	private static final String IN_TYPE = "inType";
//	private static final String SPECIES = "species";
//
//	public static final boolean DEBUG = true;
//
//	public final static void addCleanedStrValueToList(final List<String> ids,
//			final Object v) {
//		if ((ids != null) && (v != null)) {
//			String v_str = (String) v;
//			if (v_str != null) {
//				v_str = v_str.trim();
//				if (v_str.length() > 0) {
//					ids.add(v_str);
//				}
//			}
//		}
//	}

//	public final static void parseResponse(final String json_str,
//			final Set<String> in_types, final String target_species,
//			final String target_type,
//			final Map<String, SortedSet<String>> matched_ids,
//			final Set<String> unmatched_ids) throws IOException,
//			JsonProcessingException {
//		if (DEBUG) {
//			System.out.println("str =" + json_str);
//		}
//		final ObjectMapper mapper = new ObjectMapper();
//		final JsonNode root = mapper.readTree(json_str);
//		if (DEBUG) {
//			System.out.println("root=" + root);
//		}
//
//		final JsonNode unmatched = root.path(UNMATCHED);
//
//		final Iterator<JsonNode> unmatched_it = unmatched.elements();
//		while (unmatched_it.hasNext()) {
//			unmatched_ids.add(unmatched_it.next().asText());
//		}
//		if (!root.has(MATCHED)) {
//			throw new IOException("no " + MATCHED + " field");
//		}
//
//		final JsonNode matched = root.path(MATCHED);
//
//		final Iterator<JsonNode> matched_it = matched.elements();
//		while (matched_it.hasNext()) {
//			final JsonNode n = matched_it.next();
//			if (n.has(SPECIES)) {
//				if (target_species.equals(n.get(SPECIES).asText())) {
//					if (in_types.contains(n.get(IN_TYPE).asText())) {
//						final String in = n.get(IN).asText();
//						if (n.has(MATCHES)) {
//							final JsonNode m = n.get(MATCHES);
//							if (m.size() > 0) {
//								if (m.has(target_type)) {
//									final JsonNode target_ids = m
//											.get(target_type);
//									if (target_ids.isArray()) {
//										final Iterator<JsonNode> it = target_ids
//												.iterator();
//										while (it.hasNext()) {
//											final JsonNode target_id = it
//													.next();
//											addMappedId(matched_ids, in,
//													target_id.asText());
//										}
//									} else {
//										addMappedId(matched_ids, in,
//												target_ids.asText());
//									}
//								} else {
//									System.out.println(json_str);
//									System.out.println("m=" + m);
//									throw new IOException("no target type: "
//											+ target_type);
//								}
//							}
//						} else {
//							throw new IOException("no " + MATCHES + " field");
//						}
//					}
//				}
//			} else {
//				throw new IOException("no species: " + target_species);
//			}
//		}
//	}

//	public static final String post(final String url_str,
//			final String source_type, final String json_query)
//			throws IOException {
//		final URL url = new URL(url_str);
//		final HttpURLConnection conn = (HttpURLConnection) url.openConnection();
//		conn.setDoOutput(true);
//		conn.setRequestMethod("POST");
//		conn.setRequestProperty("Content-Type", "application/json");
//		if (DEBUG) {
//			System.out.println(json_query);
//		}
//		final OutputStream os = conn.getOutputStream();
//		os.write(json_query.getBytes());
//		os.flush();
//
//		if (conn.getResponseCode() != HttpURLConnection.HTTP_OK) {
//			if (conn.getResponseCode() == HttpURLConnection.HTTP_NOT_FOUND) {
//				throw new IOException(
//						"No mapping available for the data in this column");
//			} else {
//				throw new IOException("HTTP error code : "
//						+ conn.getResponseCode());
//			}
//		}
//
//		final BufferedReader br = new BufferedReader(new InputStreamReader(
//				(conn.getInputStream())));
//
//		final StringBuilder sb = new StringBuilder();
//		String line;
//		while ((line = br.readLine()) != null) {
//			sb.append(line);
//		}
//
//		br.close();
//		conn.disconnect();
//		os.close();
//
//		return sb.toString();
//	}

//	public final static String runQuery(final List<String> ids,
//			final String target_type, final String source_type, final String url)
//			throws IOException {
//		final String json_query = makeQuery(ids, target_type);
//		System.out.println("url=" + url);
//		System.out.println("json_query=" + json_query);
//		return post(url, source_type, json_query);
//	}

//	private final static void addMappedId(
//			final Map<String, SortedSet<String>> matched_ids, final String in,
//			final String id) {
//		if ((id != null) && (id.length() > 0)) {
//			if (!matched_ids.containsKey(in)) {
//				matched_ids.put(in, new TreeSet<String>());
//			}
//			matched_ids.get(in).add(id);
//		}
//	}

//	private final static StringBuilder listToString(final List<String> l) {
//		final StringBuilder sb = new StringBuilder();
//		boolean first = true;
//		for (final String s : l) {
//			if (first) {
//				first = false;
//			} else {
//				sb.append(",");
//			}
//			sb.append("\"");
//			sb.append(s);
//			sb.append("\"");
//		}
//		return sb;
//	}

//	private static final String makeQuery(final List<String> ids,
//			final String target_type) {
//		final StringBuilder sb = new StringBuilder();
//		sb.append("{\"ids\": [");
//		sb.append(listToString(ids));
//		sb.append("], \"idTypes\":[\"" + target_type + "\"] }");
//		return sb.toString();
//	}

	// /////

//	public SortedMap<String, SortedSet<String>> run(List<String> values) {
//		final String target = (String) getIdmapLabelTarget();
//		final String source = (String) getIdmapLabelSource();
//		final String species = (String) getIdmapLabelSpecies();
//
//		boolean source_is_list = false;
//		// if (column.getType() == List.class) {
//		// source_is_list = true;
//		// }
//
//		final List<String> ids = new ArrayList<String>();
//		for (final Object v : values) {
//			// System.out.println(v);
//			if (v != null) {
//				if (source_is_list) {
//					for (final Object lv : (List) v) {
//						addCleanedStrValueToList(ids, lv);
//					}
//				} else {
//					addCleanedStrValueToList(ids, v);
//				}
//			}
//		}
//		final SortedSet<String> in_types = new TreeSet<String>();
//		in_types.add(SYNONYMS);
//		in_types.add(source);
//
//		String res = null;
//		try {
//			res = runQuery(ids, target, source, DEFAULT_MAP_SERVICE_URL_STR);
//		} catch (final IOException e) {
//			e.printStackTrace();
//			return null;
//		}
//
//		final SortedMap<String, SortedSet<String>> matched_ids = new TreeMap<String, SortedSet<String>>();
//		final SortedSet<String> unmatched_ids = new TreeSet<String>();
//
//		try {
//			parseResponse(res, in_types, species, target, matched_ids,
//					unmatched_ids);
//		} catch (final IOException e) {
//			e.printStackTrace();
//		}
//
//		System.out.println();
//		System.out.println("Matched:");
//		for (final Entry<String, SortedSet<String>> m : matched_ids.entrySet()) {
//			System.out.println(m.getKey() + "->" + m.getValue());
//		}
//		System.out.println();
//
//		boolean all_unique = true;
//		int non_unique = 0;
//		int unique = 0;
//		int min = Integer.MAX_VALUE;
//		int max = 0;
//		for (final SortedSet<String> v : matched_ids.values()) {
//			if (v != null) {
//				if (v.size() > 1) {
//					all_unique = false;
//					++non_unique;
//					if (v.size() > max) {
//						max = v.size();
//					}
//					if (v.size() < min) {
//						min = v.size();
//					}
//				} else {
//					++unique;
//				}
//			}
//		}
//
//		boolean many_to_one = false;
//
//		final String msg;
//
//		if (matched_ids.size() < 1) {
//			msg = "Failed to map any identifier" + "\n" + "Total identifiers: "
//					+ ids.size() + "\n" + "Source type: " + source + "\n"
//					+ "Target type: " + target;
//		} else {
//			final String o2o;
//
//			if (all_unique) {
//				o2o = "All mappings one-to-one" + "\n";
//			} else {
//				o2o = "Not all mappings one-to-one:" + "\n" + "  one-to-one: "
//						+ unique + "\n" + "  one-to-many: " + non_unique
//						+ " (range: " + min + "-" + max + ")" + "\n";
//			}
//
//			final String m2o;
//			if (many_to_one) {
//				m2o = "Same/all mappings many-to-one" + "\n";
//			} else {
//				m2o = "";
//
//			}
//
//			msg = "Successfully mapped identifiers: " + matched_ids.size()
//					+ "\n" + "Total source identifiers: " + ids.size() + "\n"
//					+ o2o + m2o + "Source type: " + source + "\n"
//					+ "Target type: " + target + "\n";
//		}
//
//		return matched_ids;
//
//	}

	public void setColIdx(int colIdx) {
		this.colIdx = colIdx;

	}

}
