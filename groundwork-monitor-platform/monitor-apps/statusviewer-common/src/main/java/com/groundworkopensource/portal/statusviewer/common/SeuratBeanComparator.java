package com.groundworkopensource.portal.statusviewer.common;

import java.io.Serializable;
import java.util.Comparator;

import com.groundworkopensource.portal.statusviewer.bean.SeuratBean;

/**
 * Comparator class, used in sorting of SeuratBean objects. Used in
 * search functionality.
 * 
 * @author nitin_jadhav
 */
public class SeuratBeanComparator implements
        Comparator<SeuratBean>, Serializable {

    /**
     * Serial UID
     */
    private static final long serialVersionUID = 1L;

    /**
     * selected sorting type
     */
    private SeuratSortType sortType = SeuratSortType.ALPHA;

    /**
     * Set current sort option according to selected field in selectOneMenu.
     * 
     * @param sortType
     */
    public void setSortType(SeuratSortType sortType) {
        this.sortType = sortType;
    }

    /**
     * Comparison method of Comparator.
     * 
     * @see java.util.Comparator#compare(java.lang.Object, java.lang.Object)
     * 
     * @return integer
     * @param entity1
     * @param entity2
     */
    public int compare(SeuratBean entity1, SeuratBean entity2) {
        switch (sortType) {
            case ALPHA:
                // use entity names as sorting basis
                return entity1.getName().compareTo(entity2.getName());
            case SEVERITY:
                // use the sequence in which the SeuratStatus of both entities
                // occur in the SeuratStatusEnum as basis for sorting.
                return entity1.getSeuratStatus().compareTo(
                        entity2.getSeuratStatus());
            case STATE_CHANGE:
                // Sort according to last state change time, in which host with
                // the most recent service problems is shifted to the top left.
                long stateChangeTimeEntity1 = 0;
                long stateChangeTimeEntity2 = 0;
                switch (entity1.getSeuratStatus()) {
                    case SEURAT_HOST_TROUBLED_25P:
                    case SEURAT_HOST_TROUBLED_50P:
                    case SEURAT_HOST_TROUBLED_75P:
                    case SEURAT_HOST_TROUBLED_100P:
                        stateChangeTimeEntity1 = entity1.getLastStateChange();
                        break;
                    default:
                        break;
                }
                switch (entity2.getSeuratStatus()) {
                    case SEURAT_HOST_TROUBLED_25P:
                    case SEURAT_HOST_TROUBLED_50P:
                    case SEURAT_HOST_TROUBLED_75P:
                    case SEURAT_HOST_TROUBLED_100P:
                        stateChangeTimeEntity2 = entity2.getLastStateChange();
                        break;
                    default:
                        break;
                }
                if (stateChangeTimeEntity1 != 0 && stateChangeTimeEntity2 != 0) {
                    // both entities have "troubled services". compare there
                    // stateChangeTime
                    if (stateChangeTimeEntity1 < stateChangeTimeEntity2) {
                        return -1;
                    } else if (stateChangeTimeEntity1 > stateChangeTimeEntity2) {
                        return 1;
                    }
                    return 0;
                    // else on of the entities is having troubled. services.
                    // pull it up in sequence.
                } else if (stateChangeTimeEntity1 != 0) {
                    return -1;
                } else if (stateChangeTimeEntity2 != 0) {
                    return 1;
                } else {
                    // ...else we don't really care about them.
                    return 0;
                }
            default:
                break;
        }
        return 0;
    }
}
