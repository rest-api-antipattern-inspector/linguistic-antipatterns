package com.sofa.metric.lexical;

import java.util.ArrayList;
import java.util.List;

import net.didion.jwnl.JWNLException;
import net.didion.jwnl.data.PointerType;
import net.didion.jwnl.data.Synset;
import net.didion.jwnl.data.list.PointerTargetNode;
import net.didion.jwnl.data.list.PointerTargetNodeList;
import net.didion.jwnl.data.relationship.Relationship;
import net.didion.jwnl.data.relationship.RelationshipFinder;
import net.didion.jwnl.data.relationship.RelationshipList;
import edu.stanford.nlp.trees.GrammaticalStructure;
import edu.stanford.nlp.trees.TypedDependency;

public class HierarchicalMetrics {
	
	/**
	 * Specialization into two nodes occurs when a node contains the same word as it predecessor with adjectives
	 * @param nodeA
	 * @param nodeB
	 * @param reliability
	 * @return
	 * @throws JWNLException 
	 */
	public static boolean specializationHierachy(ArrayList<String> nodeA, ArrayList<String> nodeB) throws JWNLException {
		
		// retrieves discriminant words
		ArrayList<String> discriminantsA = StanfordPOS.getNodeDiscriminants(nodeA);
		ArrayList<String> discriminantsB = StanfordPOS.getNodeDiscriminants(nodeB);
		
		for (String wordA : discriminantsA) {
			if (discriminantsB.contains(wordA)) {
				
				// retrieves grammatical structure for the node B
				GrammaticalStructure gmstruct = Dictionaries.getInstance().getGrammaticalStructureForNode(nodeB);
				List<TypedDependency> dependencies = (List<TypedDependency>) gmstruct.typedDependenciesCollapsed();
				// checks dependencies
				for (TypedDependency dependency : dependencies) {
					if (specializationRelations.contains(dependency.reln().getShortName())) {
						if (dependency.gov().label().originalText().equals(wordA)) {
							return true;
						}
					}
				}		
			}
		}
		return false;
	}
	
	private static ArrayList<String> specializationRelations = createSpecializationRelations();
	private static ArrayList<String> createSpecializationRelations() {
		ArrayList<String> result = new ArrayList<String>();
		result.add("advmod");
		result.add("amod");
		result.add("appos");
		return result;
	}
	
	/**
	 * Detect reversed hierarchy between two nodes
	 * @param nodeA
	 * @param nodeB
	 * @return
	 */
	public static String reversedHierarchy(ArrayList<String> nodeA, ArrayList<String> nodeB) {
		
		// retrieves senses
		ArrayList<Synset> sensesA = Dictionaries.getInstance().getWNSenses(nodeA);
		ArrayList<Synset> sensesB = Dictionaries.getInstance().getWNSenses(nodeB);
		
		for (Synset senseA : sensesA) {
			for (Synset senseB : sensesB) {
				for (PointerType type : reversedHierarchyRelationTypes) {
					try {
						// finds relations
						RelationshipList relations = RelationshipFinder.getInstance().findRelationships(senseA, senseB, type);
						
						for (int i = 0; i < relations.size(); i++) {
							// checks the last node relation type
							Relationship relation = (Relationship) relations.get(i);
							PointerTargetNodeList list = relation.getNodeList();
							if (list.size() > 0) {
								PointerTargetNode node = (PointerTargetNode) list.get(list.size() - 1);
								if (reversedHierarchyRelationTypes.contains(node.getType())) {
									return type.getLabel();
								}
							}
						}
						
					} catch (JWNLException except) {
						except.printStackTrace();
					}
				}
			}
		}
		
		return "";
	}
	
	private static ArrayList<PointerType> reversedHierarchyRelationTypes = createReversedHierarchyRelationTypes();
	private static ArrayList<PointerType> createReversedHierarchyRelationTypes() {
		ArrayList<PointerType> result = new ArrayList<PointerType>();
		result.add(PointerType.HYPONYM);
		result.add(PointerType.CATEGORY_MEMBER);
		result.add(PointerType.DERIVED);
		result.add(PointerType.ENTAILED_BY);
		result.add(PointerType.INSTANCES_HYPONYM);
		result.add(PointerType.REGION_MEMBER);
		return result;
	}
}