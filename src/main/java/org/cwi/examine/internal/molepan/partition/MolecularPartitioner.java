package org.cwi.examine.internal.layout.mp;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

import org.cwi.examine.internal.data.Network;



public class MolecularPartitioner {
			 public Network network;



	public  MolecularPartitioner(Network network){
			this.network = network;
	
		SSSR sssr = new SSSR(network);	

	}
}




