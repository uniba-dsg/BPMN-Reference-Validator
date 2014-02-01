package de.uniba.wiai.lspi.ws1213.ba.application;

import java.io.File;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Properties;

import org.jdom2.Element;

/**
 * This is a tree data structure for BPMN files. For the structure it contains a
 * parent tree reference until it is the root and child trees until it is a
 * leaf. For the content it contains the paths (inclusive name) of the
 * containing files. It also contains the elements of the files hashed by their
 * id. More than one file is only possible through the joining method, which is
 * for files with the same namespace. It does not prevent cyclic structures!<br />
 * Example for use:<br />
 * <code> ... <br />
 * BPMNFileTree root = new BPMNFileTree(null, "path/to/root.bpmn", language);<br />
 * root.setElemens(elementsRoot);<br />
 * BPMNFileTree leaf1 = new BPMNFileTree(root, "path/to/leaf1.bpmn", language);<br />
 * leaf1.setElemens(elementsLeaf1);<br />
 * BPMNFileTree leaf2 = new BPMNFileTree(root, "path/to/leaf2.bpmn", language);<br />
 * leaf2.setElemens(elementsLeaf2);<br />
 * root.addChild(leaf1);<br />
 * root.addChild(leaf2);<br />
 * ...</code>
 * 
 * @author Andreas Vorndran
 * @version 1.0
 * 
 */
public class BPMNFileTree {

	private BPMNFileTree parentTree;
	private List<BPMNFileTree> childTrees;

	private List<File> files;
	private HashMap<String, Element> elements;

	private Properties language;

	/**
	 * Constructor
	 * 
	 * @param parentTree
	 *            the parent tree or null if it is the root
	 * @param path
	 *            the path to the containing file (inclusive file name)
	 * @param language
	 *            the reference to the language properties (used for toString
	 *            method)
	 */
	public BPMNFileTree(BPMNFileTree parentTree, String path,
			Properties language) {
		this.parentTree = parentTree;
		files = new ArrayList<>();
		files.add(new File(path));
		this.language = language;
		elements = new HashMap<>();
	}

	/**
	 * This method adds a child tree to the current tree.
	 * 
	 * @param child
	 *            the BPMN file tree to add
	 */
	public void addChild(BPMNFileTree child) {
		if (childTrees == null) {
			childTrees = new ArrayList<>();
		}
		childTrees.add(child);
	}

	/**
	 * This method checks if the file is in the tree.
	 * 
	 * @param fileName
	 *            the name of the file to search for (with type ending)
	 * @return true if the file was found
	 */
	public boolean contains(String fileName) {
		for (File file : files) {
			if (file.getName().equals(fileName)) {
				return true;
			}
		}
		if (childTrees == null || childTrees.size() == 0) {
			return false;
		} else {
			for (BPMNFileTree child : childTrees) {
				if (child.contains(fileName)) {
					return true;
				}
			}
			return false;
		}
	}

	/**
	 * This method returns a recursive list of all child tree elements starting
	 * with the children of this tree. It does not contain this tree.
	 * 
	 * @return list of all child elements
	 */
	public List<BPMNFileTree> getAllChildren() {
		List<BPMNFileTree> children = new ArrayList<>();
		if (childTrees != null) {
			for (BPMNFileTree bpmnFileTree : childTrees) {
				children.add(bpmnFileTree);
				children.addAll(bpmnFileTree.getAllChildren());
			}
		}
		return children;
	}

	/**
	 * This method searches in the elements of this file and in the elements of
	 * all children for the element with the given id and returns it.
	 * 
	 * @param id
	 *            the id of the element to get
	 * @return the searched element or null
	 */
	public Element getElement(String id) {
		Element element = elements.get(id);
		if (element == null) {
			for (BPMNFileTree child : childTrees) {
				element = child.getElement(id);
				if (element != null) {
					break;
				}
			}
		}
		return element;
	}

	/**
	 * This method returns the list of file paths the elements in this tree node
	 * are from.
	 * 
	 * @return list of absolute paths
	 */
	public List<String> getPaths() {
		List<String> paths = new ArrayList<>();
		for (File file : files) {
			paths.add(file.getAbsolutePath());
		}
		return paths;
	}

	/**
	 * This method checks if this tree is a leaf.
	 * 
	 * @return true if no children are available
	 */
	public boolean isLeaf() {
		if (childTrees == null || childTrees.size() == 0) {
			return true;
		}
		return false;
	}

	/**
	 * This method joins the given elements of a file with the same namespace to
	 * the elements of this tree.
	 * 
	 * @param newElements
	 *            the elements to join
	 * @param path
	 *            the path of the file to join
	 * @param force
	 *            overwrites existing keys
	 * @return true if successful, false if not forced and key overlap between
	 *         the hash maps
	 */
	public boolean joinElements(HashMap<String, Element> newElements,
			String path, boolean force) {
		if (!force) {
			for (String key : newElements.keySet()) {
				if (elements.containsKey(key)) {
					return false;
				}
			}
		}
		elements.putAll(newElements);
		files.add(new File(path));
		return true;
	}

	/**
	 * @return the parentTree
	 */
	public BPMNFileTree getParentTree() {
		return parentTree;
	}

	/**
	 * @return the childTrees
	 */
	public List<BPMNFileTree> getChildTrees() {
		return childTrees;
	}

	/**
	 * @return the elements
	 */
	public HashMap<String, Element> getElements() {
		return elements;
	}

	/**
	 * @param elements
	 *            the elements to set
	 */
	public void setElements(HashMap<String, Element> elements) {
		this.elements = elements;
	}

	@Override
	public String toString() {
		String returnString = "<";
		for (File file : files) {
			returnString = returnString + file.getName() + "; ";
		}
		returnString = returnString.substring(0, returnString.length() - 2);
		if (parentTree != null) {
			returnString = returnString + " |"
					+ language.getProperty("bpmnfiletree.parent")
					+ parentTree.getPaths().get(0);
		}
		if (childTrees != null) {
			returnString = returnString + " |"
					+ language.getProperty("bpmnfiletree.children");
			for (BPMNFileTree child : childTrees) {
				returnString = returnString + " <> " + child.getPaths().get(0);
			}
		}
		return returnString + ">";
	}
}
