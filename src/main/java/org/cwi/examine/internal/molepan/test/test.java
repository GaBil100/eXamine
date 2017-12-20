/*MARKER********************************************************************
****************************************************************************
****************************************************************************
*						'Testing' Class									   *
****************************************************************************
****************************************************************************
****************************************************************************
****************************************************************************
****************************************************************************/

public  class test{
	public static void main (String[] args){
/*
	int n = Integer.parseInt(args[0]);
	double y = 0.5;  //Node_0  x
	double x = 0.5;	 //Node_0  y

	double r = 2;
	double g = Math.PI - (2*Math.PI/n);
	double a = 2*Math.PI-g;
	double[][] P = new double[2][n];
	double b = 0;	*/

	
/*MARKER********************************************************************
****************************************************************************
****************************************************************************
*						'Testing' CDK Coordinates									   *
****************************************************************************
****************************************************************************
****************************************************************************
****************************************************************************
****************************************************************************/
	/*
	
2D:[(-1.0199999999999996, -0.17817352384164786)]

2D:[(0.47999999999999954, -0.17817352384165264)]

2D:[(1.2300000000000026, 1.1208645818350038)],

2D:[(1.4904722665004, 2.598076211353315)]


2D:[(-0.17953893117885888, 1.6338947968235102)]

2D:[(1.229999999999996, -1.4772116295183122)],

2D:[(2.730000000000003, 1.1208645818350027)]

2D:[(4.030000000000004, 1.8708645818350016)],

2D:[(4.030000000000003, 0.3708645818350018)],

	*/
	
			StdDraw.setPenRadius(0.01);
			StdDraw.setPenColor(StdDraw.BLACK);
			
		
	
	
		double y = 0.2;
	double x = 0; 
	
	
StdDraw.text(2.05/20+x, 5.798917918952455/20+y, "1");
StdDraw.text(0.75/20+x, 5.038917918952455/20+y, "2");
StdDraw.text(0.75/20+x, 3.538917918952455/20+y, "3");
StdDraw.text(2.05/20+x, 2.798917918952455/20+y, "4");
StdDraw.text(3.34/20+x, 3.538917918952455/20+y, "5");
StdDraw.text(3.34/20+x, 5.038917918952455/20+y, "6");
StdDraw.text(4.640000000000001/20+x, 5.798917918952455/20+y, "7");
StdDraw.text(5.9399999999999995/20+x, 5.038917918952455/20+y, "8");
StdDraw.text(5.9399999999999995/20+x, 3.538917918952455/20+y, "9");
StdDraw.text(4.640000000000001/20+x, 2.798917918952455/20+y, "10");
StdDraw.text(5.9399999999999995/20+x, 2.048917918952455/20+y, "11");
StdDraw.text(6.690208131003807/20+x, 0.7500000000000004/20+y, "12");
StdDraw.text(10.788284342357125/20+x, 5.049218485895393/20+y, "13");
StdDraw.text(10.788284342357123/20+x, 3.5492184858953926/20+y, "14");
StdDraw.text(9.489246236680463/20+x, 2.799218485895395/20+y, "15");
StdDraw.text(8.190208131003807/20+x, 3.5492184858953983/20+y, "16");
StdDraw.text(12.087322448033781/20+x, 2.7992184858953912/20+y, "17");
StdDraw.text(13.58815547295355/20+x, 2.79873753873372/20+y, "18");
StdDraw.text(12.83815547295355/20+x, 1.499699433057062/20+y, "19");
			
			
			



}  }        
/*
		for(int i=0;i<n;i++){
			b = (i*2*Math.PI/n);
			a = a + g;
	
			P[0][i] = x + Math.cos(b);
			P[1][i] = y + Math.sin(b);
			
				StdDraw.setPenRadius(0.001);
				StdDraw.setPenColor(StdDraw.BLACK);
				
				StdDraw.line(x/10+0.5,y/10+0.5,(P[0][i]/10)+0.5,(P[1][i]/10)+0.5);
				
			StdDraw.setPenRadius(0.01);	
			if(i>1)StdDraw.setPenColor(StdDraw.RED);
			else if (i==1)StdDraw.setPenColor(StdDraw.GREEN);
			else StdDraw.setPenColor(StdDraw.BLUE);
			StdDraw.point((P[0][i]/10)+0.5, (P[1][i]/10)+0.5);
			x = P[0][i];
			y = P[1][i];
		 }
		 
		 	
		 	
		 for(int i=0;i<4;i++){
					//if (i>-1){a = a + g;}
			P[0][i] = x + Math.cos(Math.pow(-1,i)*120);
			P[1][i] = y + Math.sin(Math.pow(-1,i)*120);
		StdDraw.setPenRadius(0.001);
				StdDraw.setPenColor(StdDraw.BLACK);
				
				StdDraw.line(x/10+0.5,y/10+0.5,(P[0][i]/10)+0.5,(P[1][i]/10)+0.5);
			
			StdDraw.point((P[1][i]/10)+0.5, (P[0][i]/10)+0.5);
			x = P[0][i];
			y = P[1][i];
		 }
		
		
	}
   
}
         
            // Vertex positions start at (0,0), or at position of previous layout.
            P = new double[2][vN];
            for(int i = 0; i < nodes.length; i++) {
                PVector pos = oldLayout == null ? PVector.v() : oldLayout.position(richNodes[i]);
                P[0][i] = pos.x;   // Koordinatenvergabe?
                P[1][i] = pos.y;
            }
            
            // Gradient descent. 			// Minum
            G = new double[vN][vN];
            for(int i = 0; i < vN; i++)
                for(int j = i; j < vN; j++)
                    G[i][j] = G[j][i] =
                            extRichGraph.containsEdge(richNodes[i], richNodes[j]) ||
                            network.graph.containsEdge(richNodes[i].element, richNodes[j].element) ? 1 : 2;
            descent = new Descent(P, D, null);
            
            // Apply initialIterations without user constraints or non-overlap constraints.
            descent.run(INITIAL_ITERATIONS);
            
            // Initialize vertex and contour bound respecting projection.
            // TODO: convert to rich graph form.
            descent.project = new BoundProjection(radii, mD).projectFunctions();
            
            // Allow not immediately connected (by direction) nodes to relax apart (p-stress).
            descent.G = G;
            descent.run(PHASE_ITERATIONS);
            
            converged = false;
        }
        // Improve layout.
        else {
            converged = descent.run(PHASE_ITERATIONS);
        }
        
        // Measure span and shift nodes top left to (0,0).
        double minX = Double.MAX_VALUE;
        double minY = Double.MAX_VALUE;
        double maxX = Double.MIN_VALUE;
        double maxY = Double.MIN_VALUE;
        for(int i = 0; i < vN; i++) {
            minX = Math.min(minX, P[0][i]);
            minY = Math.min(minY, P[1][i]);
            maxX = Math.max(maxX, P[0][i]);
            maxY = Math.max(maxY, P[1][i]);
        }
        this.dimensions = PVector.v(maxX - minX, maxY - minY);
        
        for(int i = 0; i < vN; i++) {
            P[0][i] -= minX;
            P[1][i] -= minY;
        }
        
        return converged;
    }
    
    */
