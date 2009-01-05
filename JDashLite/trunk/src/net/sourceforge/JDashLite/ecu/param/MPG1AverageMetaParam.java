/*********************************************************
 * 
 * @author spowell
 * CtoFMetaParam.java
 * Sep 20, 2008
 *
Copyright (C) 2008 Shane Powell

This library is free software; you can redistribute it and/or
modify it under the terms of the GNU Lesser General Public
License as published by the Free Software Foundation; either
version 2.1 of the License, or (at your option) any later version.

This library is distributed in the hope that it will be useful,
but WITHOUT ANY WARRANTY; without even the implied warranty of
MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the GNU
Lesser General Public License for more details.

You should have received a copy of the GNU Lesser General Public
License along with this library; if not, write to the Free Software
Foundation, Inc., 51 Franklin Street, Fifth Floor, Boston, MA  02110-1301  USA
 *
 *********************************************************/

package net.sourceforge.JDashLite.ecu.param;

import net.sourceforge.JDashLite.ecu.comm.ECUParameter;
import net.sourceforge.JDashLite.ecu.comm.ValueChangedListener;

/*********************************************************
 * This MPG average parameter is based on the MPG1MetaParam.
 * As in, the calculation to generate the actual MPG value is the
 * one used in the MPG1MetaParam object.  But, this averaging
 * parameter takes the 2 pysical values (MAF/KPH) and keeps a
 * cummulative running average of each of these.  These to averages
 * are then used in the MPG calculation to create an Average MPG.
 *
 * @see MPG1MetaParam
 *********************************************************/
public class MPG1AverageMetaParam extends MultiParamMetaParameter
{
	
	private static final String DESC = "Based on the simplest and fastes of the MPG calculations, this parameter keeps a running average of your vehicles " +
			"MAF and KPG values.  These averaged values are then used to calculate your average MPG. " +
			"This one depends only on your cars MAF and KPH values.  So, your car will need a MAF sensor. " +
			"If your care does not have a MAF, don't worry.  Your ECU might calculate the MAF value " +
			"and provide it anyway.  My 2000 Subaru does not have a MAF, but the ECU does indeed provide a MAF value.";
	private static final int MAF_NDX = 0;
	private static final int KPH_NDX = 1;
	
	private long mafCount_ = 0;
	private long kphCount_ = 0;
	private double mafAverage_ = 0.0;
	private double kphAverage_ = 0.0;
	
	private MPG1MetaParam mpgParam_ = new MPG1MetaParam(null, null, null);
	
	/********************************************************
	 * 
	 *******************************************************/
	public MPG1AverageMetaParam(String name, ECUParameter maf, ECUParameter kph)
	{
		super(name, new ECUParameter[] {maf, kph});
		
		
		
		/* Listen for value changes in each MAF/KPH parameter */
		maf.addValueChangedListener(new ValueChangedListener()
		{
			public void onValueChanged()
			{
				double newValue = params_[MAF_NDX].getValue();
				double currentAverage = mafAverage_;
				mafCount_ += 1;
				mafAverage_ = currentAverage + ((newValue - currentAverage) / (mafCount_));
				
			}
		});
		
		kph.addValueChangedListener(new ValueChangedListener()
		{
			public void onValueChanged()
			{
				double newValue = params_[KPH_NDX].getValue();
				double currentAverage = kphAverage_;
				kphCount_ += 1;
				kphAverage_ = currentAverage + ((newValue - currentAverage) / (kphCount_));
			}
		});
		
		
	}
	
	/*********************************************************
	 * (non-Javadoc)
	 * @see net.sourceforge.JDashLite.ecu.comm.ECUParameter#getValue()
	 ********************************************************/
	public double getValue()
	{
		
		if (this.mafCount_ > 0 && this.kphCount_ > 0)
		{
			return this.mpgParam_.calculateMPG(this.mafAverage_, this.kphAverage_);
		}
		else
		{
			return 0.0;
		}
	}
	
	/*********************************************************
	 * (non-Javadoc)
	 * @see net.sourceforge.JDashLite.ecu.comm.ECUParameter#getLabel()
	 ********************************************************/
	public String getLabel()
	{
		return "MPG 1";
	}

	
	/*********************************************************
	 * (non-Javadoc)
	 * @see net.sourceforge.JDashLite.ecu.comm.ECUParameter#getDescription()
	 ********************************************************/
	public String getDescription()
	{
		return DESC; 
	}
	
	

}
