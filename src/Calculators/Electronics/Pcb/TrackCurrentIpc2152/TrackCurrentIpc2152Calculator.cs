﻿using System;
using System.Collections.Generic;
using System.Linq;
using System.Text;
using System.Threading.Tasks;
using System.Windows.Controls;

using NinjaCalc.Core;
using NinjaCalc.Calculators.Electronics.Pcb.TrackCurrentIpc2152;

namespace NinjaCalc.Calculators.Electronics.Pcb.TrackCurrentIpc2152 {

    class TrackCurrentIpc2152Calculator : Calculator {


        const double NUM_MILS_PER_MM = 1000/25.4;
	    const double UNIT_CONVERSION_COPPER_THICKNESS_M_PER_OZ = 0.0000350012;
	    const double UNIT_CONVERSION_M_PER_MIL = 25.4/1e6;
	    const double UNIT_CONVERSION_M2_PER_MIL2 = UNIT_CONVERSION_M_PER_MIL*UNIT_CONVERSION_M_PER_MIL;

	    const double UNIT_CONVERSION_THERMAL_CONDUCTIVITY_WATT_nMETER_nKELVIN_PER_BTU_nHOUR_nFT_nDEGF = 1.73;

	    // UNIVERSAL CHART CONSTANTS

	    // The trendlines to calculate the co-efficients for a fixed temp takes the form y = Ax^B
	    // where y is the co-efficient, x is the temperature.
	    // e.g. (co-efficient A) = AA * temp ^ AB
	    //      (co-efficient B) = BA * temp ^ BB
	    const double UNIVERSAL_CHART_TREND_LINE_COEF_AA = 8.9710902134e-02;
	    const double UNIVERSAL_CHART_TREND_LINE_COEF_AB = 3.9379253898e-01;

	    const double UNIVERSAL_CHART_TREND_LINE_COEF_BA = 5.0382053698e-01;
	    const double UNIVERSAL_CHART_TREND_LINE_COEF_BB = 3.8495772461e-02;

	    // TRACK THICKNESS MODIFIER CONSTANTS

	    // The data from the track thickness modifier graph in IPS-2152 is modelled using
	    // a 5th degree polynomial

	    // y = C0 + C1*x^1 + C2*x^2 + C3*x^3 + C4*x^4 + C5*x^5

	    static readonly double[,] TRACK_THICKNESS_TREND_LINE_COEF_COEF_A =
        {
		    {
			    9.8453567795e-01,	// C0C0
			    -2.2281787548e-01,	// C0C1
			    2.0061423196e-01,	// C0C2
			    -4.1541116264e-02,	// C0C3
		    },
		    {
			    -1.6571949210e-02,	// C1C0
			    1.7520059279e-04,	// C1C1
			    -5.0615234096e-03,	// C1C2
			    2.2814836340e-03,	// C1C3
		    },
		    {
			    8.8711317661e-04,	// C2C0
			    1.3631745743e-03,	// C2C1
			    -2.2373309710e-04,	// C2C2
			    -1.0974218613e-04	// C2C3
		    },
		    {
			    -6.6729255031e-06,	// e.t.c...
			    -1.4976736827e-04,
			    5.8082340133e-05,
			    -2.4728159584e-06
		    },
		    {
			    -7.9576264561e-07,	
			    5.5788354958e-06,	
			    -2.4912026388e-06,	
			    2.4000295954e-07	
		    },
		    {
			    1.6619678738e-08,	
			    -7.1122635445e-08,	
			    3.3800191741e-08,	
			    -3.9797591878e-09	
		    }
        };

	    // BOARD THICKNESS CONSTANTS

        const double BOARD_THICKNESS_TREND_LINE_COEF_A = 2.4929779905e+01;
        const double BOARD_THICKNESS_TREND_LINE_COEF_B = -7.5501997929e-01;

	    // PLANE PROXIMITY CONSTANTS

        const double PLANE_PROXIMITY_TREND_LINE_COEF_M = 3.1298662911e-03;
        const double PLANE_PROXIMITY_TREND_LINE_COEF_C = 4.0450883823e-01;

	    // THERMAL CONDUCTIVITY CONSTANTS

        const double THERMAL_CONDUCTIVITY_TREND_LINE_COEF_M = -1.4210148167e+00;
        const double THERMAL_CONDUCTIVITY_TREND_LINE_COEF_C = 1.1958174134e+00;


        CalcVarNumericalInput TrackCurrent {
            get;
            set;
        }

        CalcVarNumericalInput TempRise {
            get;
            set;
        }

        CalcVarNumericalOutput UnadjustedTrackCrossSectionalArea {
            get;
            set;
        }

        CalcVarNumericalInput TrackThickness {
            get;
            set;
        }

        CalcVarComboBox TrackLayer {
            get;
            set;
        }

        CalcVarNumericalOutput MinTrackWidth {
            get;
            set;
        }

        //===============================================================================================//
        //========================================== CONSTRUCTORS =======================================//
        //===============================================================================================//

        public TrackCurrentIpc2152Calculator()
            : base(
            "Track Current (IPC-2152)",
            "PCB track current carrying capability calculator, using the IPC-2152 standard.",
            "pack://application:,,,/Calculators/Electronics/Pcb/TrackCurrentIpc2221A/grid-icon.png",
            new string[] { "Electronics", "PCB" },
            new string[] { "pcb, track, current, trace, width, carry, heat, temperature, ipc, ipc2221a, ipc-2221a" },
            new TrackCurrentIpc2152View()) {

            // Re-cast the view so we can access it's unique properties
            TrackCurrentIpc2152View view = (TrackCurrentIpc2152View)this.View;

            //===============================================================================================//
            //===================================== TRACE CURRENT (input) ===================================//
            //===============================================================================================//
            
           this.TrackCurrent = new CalcVarNumericalInput(
                "traceCurrent",
                view.TrackCurrentValue,
                view.TrackCurrentUnits,                                                  
                new NumberUnit[]{
                    new NumberUnit("uA", 1e-6),
                    new NumberUnit("mA", 1e-3),
                    new NumberUnit("A", 1e0, NumberPreference.DEFAULT),
                },
                null);

            //========== VALIDATORS ===========//
            this.TrackCurrent.AddValidator(Validator.IsNumber(CalcValidationLevels.Error));
            this.TrackCurrent.AddValidator(Validator.IsGreaterThanZero(CalcValidationLevels.Error));
            this.TrackCurrent.AddValidator(
                new Validator(() => {
                    return ((this.TrackCurrent.RawVal < 274e-3) ? CalcValidationLevels.Warning : CalcValidationLevels.Ok);
                },
                "Current is below the minimum value (274mA) extracted from the universal graph in IPC-2152." +
                " Results might not be as accurate (extrapolation will occur)."));
            this.TrackCurrent.AddValidator(
                new Validator(() => {
                    return ((this.TrackCurrent.RawVal > 26.0) ? CalcValidationLevels.Warning : CalcValidationLevels.Ok);                                      
                },
                "Current is above the maximum value (26A) extracted from the universal graph in IPC-2152." +
                " Results might not be as accurate (extrapolation will occur)."));

            this.CalcVars.Add(this.TrackCurrent);

            //===============================================================================================//
            //====================================== TEMP RISE (input) ======================================//
            //===============================================================================================//
            
            this.TempRise = new CalcVarNumericalInput(
                "tempRise",
                view.TempRiseValue,
                view.TempRiseUnits,                                                
                new NumberUnit[]{
                    new NumberUnit("°C", 1e0, NumberPreference.DEFAULT),                        
                },
                null);

            //========== VALIDATORS ==========//
            this.TempRise.AddValidator(Validator.IsNumber(CalcValidationLevels.Error));
            this.TempRise.AddValidator(Validator.IsGreaterThanZero(CalcValidationLevels.Error));
            this.TempRise.AddValidator(
                new Validator(() => {
                    return ((this.TempRise.RawVal < 1.0) ? CalcValidationLevels.Warning : CalcValidationLevels.Ok);
                },
                "Temp. rise is below the minimum value (1°C) extracted from the universal graph in IPC-2152." +
                " Results might not be as accurate (extrapolation will occur)."));
            this.TempRise.AddValidator(
                new Validator(() => {
                    return ((this.TempRise.RawVal > 100.0) ? CalcValidationLevels.Warning : CalcValidationLevels.Ok);
                },
                "Temp. rise is above the maximum value (100°C) extracted from the universal graph in IPC-2152." +
                " Results might not be as accurate (extrapolation will occur)."));

            this.CalcVars.Add(this.TempRise);

            //===============================================================================================//
            //============================ UN-ADJUSTED TRACK CROSS-SECTIONAL AREA (output) ==================//
            //===============================================================================================//

            this.UnadjustedTrackCrossSectionalArea = new CalcVarNumericalOutput(
                "unadjustedTrackCrossSectionalArea",
                view.UnadjustedTrackCrossSectionalAreaValue,
                view.UnadjustedTrackCrossSectionalAreaUnits,
                () => {
                    
                    // Read in variables
                    var trackCurrent = this.TrackCurrent.RawVal;
                    var tempRise = this.TempRise.RawVal;

                    // Lets calculate the two co-efficients for the fixed-temp trend line 
                    var universalChartTrendLineCoefA = UNIVERSAL_CHART_TREND_LINE_COEF_AA * Math.Pow(tempRise, UNIVERSAL_CHART_TREND_LINE_COEF_AB);
                    var universalChartTrendLineCoefB = UNIVERSAL_CHART_TREND_LINE_COEF_BA * Math.Pow(tempRise, UNIVERSAL_CHART_TREND_LINE_COEF_BB);

                    // Now we know the two co-efficients, we can use the trend line eq. y=Ax^B to find the unadjusted cross-sectional area
                    var unadjustedTrackCrosssectionalAreaMils2 = Math.Pow(trackCurrent / universalChartTrendLineCoefA, 1 / universalChartTrendLineCoefB);

                    //console.log("unadjustedTrackCrosssectionalAreaMils2 = '" + unadjustedTrackCrosssectionalAreaMils2 + "'.");

                    // Convert mils^2 to m^2 (store variable values in SI units)
                    var unadjustedTrackCrosssectionalAreaM2 = unadjustedTrackCrosssectionalAreaMils2 * (1 / (NUM_MILS_PER_MM * NUM_MILS_PER_MM * 1e6));

                    return unadjustedTrackCrosssectionalAreaM2;  
                   
                },
                new NumberUnit[]{
                    new NumberUnit("um", 1e-6, NumberPreference.DEFAULT),     
                    new NumberUnit("mils\xb2", UNIT_CONVERSION_M2_PER_MIL2),
                    new NumberUnit("mm", 1e-3),                        
                });

            // Add validators
            this.UnadjustedTrackCrossSectionalArea.AddValidator(Validator.IsNumber(CalcValidationLevels.Error));
            this.UnadjustedTrackCrossSectionalArea.AddValidator(Validator.IsGreaterThanZero(CalcValidationLevels.Error));

            this.CalcVars.Add(this.UnadjustedTrackCrossSectionalArea);












            //===============================================================================================//
            //======================================== TRACK THICKNESS ======================================//
            //===============================================================================================//
            
            this.TrackThickness = new CalcVarNumericalInput(
                "trackThickness",
                view.TrackThicknessValue,
                view.TrackThicknessUnits,                
                new NumberUnit[]{
                    new NumberUnit("um", 1e-6, NumberPreference.DEFAULT),                        
                    new NumberUnit("mm", 1e-3),                        
                },
                null);

            //===== VALIDATORS =====//
            this.TrackThickness.AddValidator(Validator.IsNumber(CalcValidationLevels.Error));
            this.TrackThickness.AddValidator(Validator.IsGreaterThanZero(CalcValidationLevels.Error));
            this.TrackThickness.AddValidator(
               new Validator(() => {
                   return ((this.TrackThickness.RawVal < 17.5e-6) ? CalcValidationLevels.Warning : CalcValidationLevels.Ok);
               },
               "Track thickness is below the recommended minimum (17.5um or 0.5oz). Equation will not be as accurate (extrapolation will occur)."));
            this.TrackThickness.AddValidator(
                new Validator(() => {
                    return ((this.TrackThickness.RawVal > 105.0036e-6) ? CalcValidationLevels.Warning : CalcValidationLevels.Ok);
                },
                "Track thickness is above the recommended maximum (105um or 3oz). Equation will not be as accurate (extrapolation will occur)."));

            this.CalcVars.Add(this.TrackThickness);

            //===============================================================================================//
            //========================================= TRACK LAYER =========================================//
            //===============================================================================================//

            this.TrackLayer = new CalcVarComboBox(
                "trackLayer",
                view.TrackLayer,
                new string[] {
                    "Internal",
                    "External",
                });

            this.CalcVars.Add(this.TrackLayer);

            //===============================================================================================//
            //======================================== MIN. TRACK WIDTH =====================================//
            //===============================================================================================//
            
            this.MinTrackWidth = new CalcVarNumericalOutput(
                "minTrackWidth",
                view.MinTrackWidthValue,
                view.MinTrackWidthUnits,
                () => {
                    //Console.WriteLine("Equation() called for MinTrackWidth.");
                    var traceCurrent = this.TrackCurrent.RawVal;
                    var tempRise = this.TempRise.RawVal;
                    var trackThickness = this.TrackThickness.RawVal;
                    var trackLayer = this.TrackLayer.RawVal;
                    
                    if(trackLayer == "External")     
			        {
				        Console.WriteLine("External trace selected.");
				        double crossSectionalArea = (Math.Pow((traceCurrent/(0.048*Math.Pow(tempRise, 0.44))), 1/0.725));
				        Console.WriteLine("Cross-sectional area = " + crossSectionalArea.ToString());
				        double width = (crossSectionalArea/(trackThickness*1000000.0/25.4))*(25.4/1000000.0);
				        return width;
			        }
			        else if(trackLayer == "Internal")
			        {
				        Console.WriteLine("Internal trace selected.");
				        double crossSectionalArea = (Math.Pow((traceCurrent/(0.024*Math.Pow(tempRise, 0.44))), 1/0.725));
                        Console.WriteLine("Cross-sectional area = " + crossSectionalArea.ToString());
				        double width = (crossSectionalArea/(trackThickness*1000000.0/25.4))*(25.4/1000000.0);
				        return width;
                    }
                    else {
                        System.Diagnostics.Debug.Assert(false, "Track layer was invalid (should be either External or Internal).");
                        return Double.NaN;
                    }
                },
                new NumberUnit[]{
                    new NumberUnit("um", 1e-6),                        
                    new NumberUnit("mm", 1e-3, NumberPreference.DEFAULT),                        
                });

            // Add validators
            this.MinTrackWidth.AddValidator(Validator.IsNumber(CalcValidationLevels.Error));
            this.MinTrackWidth.AddValidator(Validator.IsGreaterThanZero(CalcValidationLevels.Error));

            this.CalcVars.Add(this.MinTrackWidth);

            //===============================================================================================//
            //=========================================== VIEW CONFIG =======================================//
            //===============================================================================================//

            // Setup the top PCB layer to dissappear if "External" is selected for the track layer,
            // and visible if "Internal" is selected.
            this.TrackLayer.RawValueChanged += (sender, e) => {
                if (this.TrackLayer.RawVal == "Internal") {
                    view.TopPcb.Visibility = System.Windows.Visibility.Visible;
                }
                else if (this.TrackLayer.RawVal == "External") {
                    view.TopPcb.Visibility = System.Windows.Visibility.Collapsed;
                }
            };

            //===============================================================================================//
            //============================================== FINAL ==========================================//
            //===============================================================================================//

            this.FindDependenciesAndDependants();
            this.RecalculateAllOutputs();
            this.ValidateAllVariables();
            
        }       
    }
}
