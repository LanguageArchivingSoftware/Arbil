package nl.mpi.arbil.ui;

import java.awt.BorderLayout;
import java.awt.Component;
import java.awt.event.MouseListener;
import javax.swing.Icon;
import javax.swing.JLabel;
import javax.swing.JPanel;

/**
 * Panel that wraps any component and adds a specified icon
 * @author Twan Goosen <twan.goosen@mpi.nl>
 */
public class ArbilIconCellPanel extends JPanel {

    protected static int minWidthForIcon = 120;
    private Icon icon;
    private JLabel iconLabel;

    /**
     *
     * @param component Component to wrap
     * @param icon Icon to shown. It will be shown at the line end
     */
    public ArbilIconCellPanel(Component component, Icon icon) {
        this(component, icon, BorderLayout.LINE_END);
    }

    /**
     *
     * @param component Component to wrap
     * @param icon Icon to shown
     * @param iconLocation Location for the icon to appear relative to text. Should be a BorderLayout constant
     * 
     * @see javax.swing.BorderFactory
     */
    public ArbilIconCellPanel(Component component, Icon icon, String iconLocation) {
        super();
        
        setLayout(new BorderLayout());
        add(component, BorderLayout.CENTER);

        this.icon = icon;
        iconLabel = new JLabel(icon);
        add(iconLabel, iconLocation);
        setBackground(component.getBackground());
    }

    @Override
    public void doLayout() {
        // When layout is done, check whether the icon should be shown (depending on current width)
        iconLabel.setVisible(getWidth() >= minWidthForIcon + icon.getIconWidth());
        super.doLayout();
    }
    
    public void addIconMouseListener(MouseListener mouseListener){
	iconLabel.addMouseListener(mouseListener);
    }
    
    public void removeIconMouseListener(MouseListener mouseListener){
	iconLabel.removeMouseListener(mouseListener);
    }
}
