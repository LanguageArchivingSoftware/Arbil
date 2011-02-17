package nl.mpi.arbil.ui.fieldeditors;

import java.awt.event.ActionEvent;
import java.awt.event.FocusEvent;
import java.awt.event.FocusListener;
import javax.swing.JComboBox;
import javax.swing.text.JTextComponent;
import nl.mpi.arbil.data.ArbilField;
import nl.mpi.arbil.data.ArbilVocabularies;

/**
 * Editable combo box that has the items of a controlled vocabulary in it. 
 * Use with ControlledVocabularyComboBoxEditor
 *
 * Document   : ControlledVocabularyComboBox
 * Created on : Wed Oct 07 11:07:30 CET 2009
 * @author Peter.Withers@mpi.nl
 * @author Twan.Goosen@mpi.nl
 * @see ControlledVocabularyComboBoxEditor
 */
public class ControlledVocabularyComboBox extends JComboBox {

    private ArbilField targetField;

    public ControlledVocabularyComboBox(ArbilField targetField) {
        targetField = targetField;
        ArbilVocabularies.Vocabulary fieldsVocabulary = targetField.getVocabulary();
        if (null == fieldsVocabulary || null == fieldsVocabulary.findVocabularyItem(targetField.getFieldValue())) {
            this.addItem(targetField.getFieldValue());
        }
        if (null != fieldsVocabulary) {
            for (ArbilVocabularies.VocabularyItem vocabularyListItem : fieldsVocabulary.getVocabularyItems()) {
                this.addItem(vocabularyListItem.itemDisplayName);
            }
        }

        //this.setEditable(true);

        //this.setSelectedItem(targetField.toString());
        this.setUI(new javax.swing.plaf.basic.BasicComboBoxUI());
    }


    public String getCurrentValue()
    {
        if(getEditor() != null && getEditor() instanceof ControlledVocabularyComboBoxEditor)
        {
            return ((ControlledVocabularyComboBoxEditor)getEditor()).getCurrentValue();
        } else{
            return getSelectedItem().toString();
        }
    }

    /**
     * @return the targetField
     */
    public ArbilField getTargetField() {
        return targetField;
    }
}
