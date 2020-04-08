package com.sofa.motifs;

import java.util.ArrayList;
import java.util.List;

public enum Motif {

	// Anti-patterns
		//AP_IgnoreStatusCode(new APIgnoreStatusCode()),
		//AP_ForgettingHypermedia(new APForgettingHypermedia()),
		//AP_Tunnel(new APTunnel()),
		//AP_IgnoringCaching(new APIgnoringCaching()),
		//AP_BreakingSelfDescriptiviness(new APBreakingSelfDescriptiviness()),
		//AP_IgnoringMIMEType(new APIgnoringMIMEType()),
		//AP_MisusingCookies(new APMisusingCookies()),
		//AP_AmorphousURI(new APAmorphousURI()),
		//AP_PluralisedNodes(new APPluralisedNodes()),
		//AP_ContextlessResourceNames(new APContextlessResourceNames()),
		//AP_NonHierarchicalNodes(new APNonHierarchicalNodes()),
		//AP_CRUDyURI(new APCRUDyURI()),

	//// Patterns
		//P_TidyURI(new PTidyURI()),
		//P_ContextualisedResourceNames(new PContextualisedResourceNames()),
		//P_VerblessURI(new PVerblessURI()),
		//P_PHierarchicalNodes(new PHierarchicalNodes()),
		//P_SingularisedNode(new PSingularisedNodes())
		//P_EntityEndpoint(new PEntityEndpoint()),
		//P_ContentNegotiation(new PContentNegotiation()),
		//P_ResponseCaching(new PResponseCaching()),
		//P_EntityLinking(new PEntityLinking()),
		//P_ConflictHandling(new PConflictHandling()),
		//P_EndPointRedirection(new PEndPointRedirection()),
		//P_VersionnedURI(new PVersionnedURI())
	;

	private AMotif ap;

	Motif(AMotif ap) {
		this.ap = ap;
	}

	public AMotif getImpl(){
		return this.ap;
	}

	public static List<Motif> antiPatterns(){
		return findByPrefix("AP_");
	}

	public static List<Motif> patterns(){
		return findByPrefix("P_");
	}

	private static List<Motif> findByPrefix(String prefix){
		List<Motif> motifs = new ArrayList<Motif>();
		for(Motif v : values()){
			if( v.name().startsWith(prefix)){
				motifs.add(v);
			}
		}
		return motifs;
	}

}
