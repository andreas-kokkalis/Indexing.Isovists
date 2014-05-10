package se.kth.akok.index.geometries.point;

/**
 * SAME_OBJECT_VISIBLE A visible point that is on the same edge of a polygon. OTHER_OBJECT_VISIBLE A visible point that is on another geometry. BOUNDARY_POINT One of the
 * four points that form the envelope of the scene.
 * 
 * @author Andreas Kokkalis
 * 
 */
public enum VisibleType {
	SAME_OBJECT_VISIBLE, OTHER_OBJECT_VISIBLE, BOUNDARY_POINT
}
