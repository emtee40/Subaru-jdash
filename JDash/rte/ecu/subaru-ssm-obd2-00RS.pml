<!-- ======================================================
     Including the standard Subaru ECU values, this file defines the addresses specific to the MY98-MY00
     ====================================================== -->
<parameters name="Subaru SSM/OBD-II (USDM MY00)">

    <description>Same as the Generic Subaru definition file, but can also read the 2000 Subaru DTC Codes.</description>

    <!-- The standard known subaru SSM addresses -->
    <include file="subaru-ssm-obd2-common.iml"/>

    <!-- The standard subaru DTC conversions -->
    <include file="subaru-ssm-obd2-dtc-meta.iml"/>

    
	<parameter name="E_DTC_CUR_0H" rate="10000">
		<description>Current DTC 0H</description>
		<address>
			<byte>0x00</byte>
			<byte>0x00</byte>
			<byte>0x8E</byte>
		</address>
	</parameter>
	<parameter name="E_DTC_CUR_0L" rate="10000">
		<description>Current DTC 0L</description>
		<address>
			<byte>0x00</byte>
			<byte>0x00</byte>
			<byte>0x8F</byte>
		</address>
	</parameter>
	<parameter name="E_DTC_CUR_1H" rate="10000">
		<description>Current DTC 1H</description>
		<address>
			<byte>0x00</byte>
			<byte>0x00</byte>
			<byte>0x90</byte>
		</address>
	</parameter>
	<parameter name="E_DTC_CUR_1L" rate="10000">
		<description>Current DTC 1L</description>
		<address>
			<byte>0x00</byte>
			<byte>0x00</byte>
			<byte>0x91</byte>
		</address>
	</parameter>
	<parameter name="E_DTC_CUR_2H" rate="10000">
		<description>Current DTC 2H</description>
		<address>
			<byte>0x00</byte>
			<byte>0x00</byte>
			<byte>0x92</byte>
		</address>
	</parameter>
	<parameter name="E_DTC_CUR_2L" rate="10000">
		<description>Current DTC 2L</description>
		<address>
			<byte>0x00</byte>
			<byte>0x00</byte>
			<byte>0x93</byte>
		</address>
	</parameter>
	<parameter name="E_DTC_CUR_3H" rate="10000">
		<description>Current DTC 3H</description>
		<address>
			<byte>0x00</byte>
			<byte>0x00</byte>
			<byte>0x94</byte>
		</address>
	</parameter>
	<parameter name="E_DTC_CUR_3L" rate="10000">
		<description>Current DTC 3L</description>
		<address>
			<byte>0x00</byte>
			<byte>0x00</byte>
			<byte>0x95</byte>
		</address>
	</parameter>
	<parameter name="E_DTC_CUR_4H" rate="10000">
		<description>Current DTC 4H</description>
		<address>
			<byte>0x00</byte>
			<byte>0x00</byte>
			<byte>0x96</byte>
		</address>
	</parameter>
	<parameter name="E_DTC_CUR_4L" rate="10000">
		<description>Current DTC 4L</description>
		<address>
			<byte>0x00</byte>
			<byte>0x00</byte>
			<byte>0x97</byte>
		</address>
	</parameter>
	<parameter name="E_DTC_HIST_0H" rate="10000">
		<description>Current DTC 0H</description>
		<address>
			<byte>0x00</byte>
			<byte>0x00</byte>
			<byte>0xA4</byte>
		</address>
	</parameter>
	<parameter name="E_DTC_HIST_0L" rate="10000">
		<description>Current DTC 0L</description>
		<address>
			<byte>0x00</byte>
			<byte>0x00</byte>
			<byte>0xA5</byte>
		</address>
	</parameter>
	<parameter name="E_DTC_HIST_1H" rate="10000">
		<description>Current DTC 1H</description>
		<address>
			<byte>0x00</byte>
			<byte>0x00</byte>
			<byte>0xA6</byte>
		</address>
	</parameter>
	<parameter name="E_DTC_HIST_1L" rate="10000">
		<description>Current DTC 1L</description>
		<address>
			<byte>0x00</byte>
			<byte>0x00</byte>
			<byte>0xA7</byte>
		</address>
	</parameter>
	<parameter name="E_DTC_HIST_2H" rate="10000">
		<description>Current DTC 2H</description>
		<address>
			<byte>0x00</byte>
			<byte>0x00</byte>
			<byte>0xA8</byte>
		</address>
	</parameter>
	<parameter name="E_DTC_HIST_2L" rate="10000">
		<description>Current DTC 2L</description>
		<address>
			<byte>0x00</byte>
			<byte>0x00</byte>
			<byte>0xA9</byte>
		</address>
	</parameter>
	<parameter name="E_DTC_HIST_3H" rate="10000">
		<description>Current DTC 3H</description>
		<address>
			<byte>0x00</byte>
			<byte>0x00</byte>
			<byte>0xAA</byte>
		</address>
	</parameter>
	<parameter name="E_DTC_HIST_3L" rate="10000">
		<description>Current DTC 3L</description>
		<address>
			<byte>0x00</byte>
			<byte>0x00</byte>
			<byte>0xAB</byte>
		</address>
	</parameter>
	<parameter name="E_DTC_HIST_4H" rate="10000">
		<description>Current DTC 4H</description>
		<address>
			<byte>0x00</byte>
			<byte>0x00</byte>
			<byte>0xAC</byte>
		</address>
	</parameter>
	<parameter name="E_DTC_HIST_4L" rate="10000">
		<description>Current DTC 4L</description>
		<address>
			<byte>0x00</byte>
			<byte>0x00</byte>
			<byte>0xAD</byte>
		</address>
	</parameter>
    
</parameters>
