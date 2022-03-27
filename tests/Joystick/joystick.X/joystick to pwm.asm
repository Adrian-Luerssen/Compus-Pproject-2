LIST P = PIC18F4321 F = INHX32
    #include <p18f4321.inc>
    CONFIG OSC = INTIO2
    CONFIG PBADEN = DIG
    CONFIG WDT = OFF
    
    
    readH EQU 0x01
    readL EQU 0x02
 
    position EQU 0x03
    flags EQU 0x04 
    ; 0 - joystick flag | 1 - manual mode (1 = java 0 = joy) | 2 - manual hit button pressed | 3 - mode (1 = automatic 0 = manual) |
    ; 4 - record flag (0 = normal | 1 = record) | 5 - 60s reached flag | 6 - save json ok flag (1 = can save 0 = cant save) 
    ; 7 - at end
    timeNoteL EQU 0x05 
    timeNoteH EQU 0x06
    
    savedTimeL EQU 0x07
    savedTimeH EQU 0x08
 
  
    debouce_counter EQU 0x0A
    noteLetter EQU 0x0B
 
    TABLE7s EQU 0x10
    
    Position_RAM_Notes EQU 0x40	    ; FSR0
    Position_RAM_Delay_L EQU 0x80   ; FSR1
    Position_RAM_Delay_H EQU 0xC0   ; FSR2
    
    ORG TABLE7s
    ; C, D - 0x10, 0x11
    DB b'00111001', b'01011110'
    ; E, F - 0x12, 0x13
    DB b'01111001', b'01110001'
    ; G, A - 0x14, 0x15
    DB b'01101111', b'01110111'
    ; B, c - 0x16, 0x17
    DB b'01111100', b'01011000'
    
    
    
    ORG 0x0000
    GOTO MAIN
    ORG 0x0008
    GOTO HIGH_RSI
    ORG 0x0018
    RETFIE FAST
    
    DEBOUNCE_LOOP
	; (1 + (1 + 1 + 3 + 1 + 2 + 8)*a + 2)*Fosc = 16ms
	CLRF debouce_counter,0 ; 1 cycle
	
	INNER_BOUNCE_LOOP
	MOVLW .250 ; number to count to before 16ms has passed - 1 Cycle
	SUBWF debouce_counter,0,0 ; 1 cycle
	BTFSC STATUS,Z,0 ; 3 cycles
	RETURN ; 2 cycles
	INCF debouce_counter,1,0 ; 1 cycle
	NOP
	NOP
	NOP
	NOP
	NOP
	NOP
	NOP
	NOP
	GOTO INNER_BOUNCE_LOOP ; 2 cycles
	
    
    CHANGE_MANUAL_MODE
	BCF INTCON3,INT1IF,0
	CALL DEBOUNCE_LOOP
	BTFSC PORTB,1,0 ; if pushbutton is still pressed after debouncing loop then proceed, if not return
	RETFIE FAST
	
	BTFSS flags,3,0
	BTG flags,1,0
	;GOTO MANUAL_LOOP
	RETFIE FAST
	
    CHANGE_MODE
	BCF INTCON3,INT2IF,0
	CALL DEBOUNCE_LOOP
	BTFSC PORTB,2,0 ; if pushbutton is still pressed after debouncing loop then proceed, if not return
	RETFIE FAST
	
	BTFSC flags,4,0
	RETFIE FAST
	
	BTG flags,3,0
	;GOTO MANUAL_LOOP
	RETFIE FAST
	
    SET_RECORDING
		
	BTFSC flags,3,0
	RETURN
	
	
	BTFSC flags,4,0
	GOTO OLD_REC
	CALL NEW_RECORDING
	RETURN
	
	OLD_REC
	CALL REC_OVER
	
	;BTG flags,4,0
	
	RETURN
	
	
    WIPE_RAM
	MOVLW 0x001
	MOVWF BSR,0  ; selecting bank 1 of the ram
	LFSR 0, Position_RAM_Notes
	
	WIPE_LOOP
	BTFSC FSR0H, 1,0
	RETURN
	
	CLRF INDF0,0
	INCF FSR0L,1
	BTFSC STATUS, DC,0
	INCF FSR0H,1
	GOTO WIPE_LOOP
	
    ASK_JAVA
	MOVLW 'P'
	MOVWF TXREG,0
	WAIT_TRANS_START_REC
	    BTFSS TXSTA, 1, 0
	    GOTO WAIT_TRANS_START_REC
	
	    
	CALL DEBOUNCE_LOOP
	BTFSS PIR1, RCIF, 0
	RETURN
	    
	
	MOVLW 'K'
	SUBWF RCREG,0
	BTFSC STATUS, Z, 0
	BSF flags,6,0
	
	;BTFSC flags,6,0
	;BSF LATD,7,0
	
	BTFSC RCSTA,OERR,0 
	CALL OVERRUN
	RETURN
	
	
    NEW_RECORDING
	
	CALL WIPE_RAM
	CALL ASK_JAVA
	BSF flags,4,0
	
	BSF LATC,2,0
	BCF LATC,3,0
	BCF LATC,4,0
	LFSR 0, Position_RAM_Notes
	LFSR 1, Position_RAM_Delay_L
	LFSR 2, Position_RAM_Delay_H
	;INCF FSR0L,1
	
	
	CALL INIT_TMR0_REC_IDLE
	CLRF TMR0H,0
	CLRF TMR0L,0
	RETURN
	
    RECORD_PRESSED
	BTFSC PORTB,4,0
	RETURN
	CALL DEBOUNCE_LOOP
	BTFSS PORTB,4,0
	RETURN
	
	
	CALL SET_RECORDING
	
	
	RECORD_PRESSED_LOOP
	BTFSS PORTB,4,0
	GOTO RECORD_PRESSED_LOOP
	
	;MOVFF flags,PORTA
	GOTO MAIN_LOOP
	
    
    PLAY_RECORDING
	BCF INTCON,INT0IF,0
	CALL DEBOUNCE_LOOP
	BTFSC PORTB,0,0 ; if pushbutton is still pressed after debouncing loop then proceed, if not return
	RETFIE FAST
	
	BTFSC flags,4,0
	RETFIE FAST
	
	BCF INTCON, GIE ; disable interrupts
	
	MOVLW 0x001
	MOVWF BSR,0  ; selecting bank 1 of the ram
	
	
	LFSR 0, Position_RAM_Notes
	LFSR 1, Position_RAM_Delay_L
	LFSR 2, Position_RAM_Delay_H
	
	PLAY_LOOP
	    MOVFF INDF0, position
	    MOVFF INDF1, savedTimeL
	    MOVFF INDF2,savedTimeH
	    
	    
	    INCF FSR0L,1
	    INCF FSR1L,1
	    INCF FSR2L,1
	    
	    MOVLW .8
	    CPFSLT position,0
	    GOTO DONE_PLAYING
	    
	    CLRF TMR0H,0
	    CLRF TMR0L,0
	    CALL INIT_TMR0_REC_IDLE
	    
	    DELAY_LOOP
		
		MOVFF TMR0H,readH
		MOVFF TMR0L,readL
		;MOVFF readH,LATD
		;MOVFF savedTimeH,LATA
		
		
		MOVF savedTimeH,0,0 ; 11101010
		CPFSLT readH,0
		GOTO GT_OR_EQ_DELAY
		GOTO DELAY_LOOP

		GT_OR_EQ_DELAY
		;BSF LATA,2,0
		CPFSGT readH,0
		GOTO EQ_DELAY
		GOTO DONE_DELAY

		EQ_DELAY
		;BSF LATA,3,0
		MOVF savedTimeL,0,0 ; 01100000
		CPFSLT readL,0
		GOTO DONE_DELAY
		GOTO DELAY_LOOP

	    DONE_DELAY
	    ;MOVFF position, LATA
	    ;BTG LATD,7,0
	    CALL INIT_TMR0_PWM
	    
	    CALL CONVERT_POS_TIME
	    CALL MOVE_SERVO_X
	    CALL HIT_NOTE
	    
	    

	    GOTO PLAY_LOOP
	
	DONE_PLAYING
	BCF flags,7,0
	CALL INIT_TMR0_PWM
	BSF INTCON, GIE ; re-enable interrupts
	
	RETFIE FAST
	
    HIGH_RSI
	BTFSC INTCON,INT0IF,0
	GOTO PLAY_RECORDING
	BTFSC INTCON3,INT1IF,0
	GOTO CHANGE_MANUAL_MODE
	BTFSC INTCON3,INT2IF,0
	GOTO CHANGE_MODE
	RETFIE FAST
	
	
    INIT_INTERRUPTS
	; Configure RB1 so it generates a High Priority Interrupt 
        ; when a falling edge is detected.
	BCF RCON,IPEN,0 ; no priorities so everything is high priority
	BCF INTCON2,RBPU,0 ; enable pull ups
	BCF INTCON2,INTEDG0,0 ; falling edge int1
	BCF INTCON2,INTEDG1,0 ; falling edge int1
	BCF INTCON2,INTEDG2,0 ; falling edge int1
	
	BSF INTCON2, TMR0IP,0 ; set TMR0 interrupt as high priority
	BSF INTCON, INT0IE,0 ; enable INT0 interrupt
	BSF INTCON3, INT1IE,0 ; enable INT1 interrupt
	BSF INTCON3, INT2IE,0 ; enable INT2 interrupt
	BSF INTCON, 7,0; enable global interrupts
	BSF INTCON, 6,0;

	RETURN
    INIT_EUSART
	
	MOVLW b'00100100' ; SYNCH -0 BRGH - 1 
	MOVWF TXSTA,0
	MOVLW b'10010001'   ; RX9 - 0
	MOVWF RCSTA,0
	MOVLW b'01001000' ; BRG16 - 1
	MOVWF BAUDCON,0
	MOVLW .25       ; baud rate of 9600
	MOVWF SPBRG,0
	;MOVLW b'00000000'
	;MOVWF SPBRGH,0
	CLRF SPBRGH,0
	RETURN
    
    INIT_OSC
	MOVLW b'01000010'    ; 1MHz 1us
	MOVWF   OSCCON,0 
	RETURN
	
    INIT_TMR0_PWM
	
	MOVLW b'10011000' ; 16 bit timer - no prescaler 
	MOVWF T0CON,0
	RETURN
    INIT_TMR0_REC_IDLE
	
	MOVLW b'10010111' ; 16 bit timer - 8 bit prescaler - counts to 65563ms 
	MOVWF T0CON,0
	RETURN
    INIT_PORTS
	MOVLW b'00000011'
	MOVWF ADCON0,0
	MOVLW b'00001110'
	MOVWF ADCON1,0
	MOVLW b'10001001'
	MOVWF ADCON2,0
	CLRF TRISA,0
	BSF TRISA,0,0
	CLRF TRISC,0
	BSF TRISC,6,0
	BSF TRISC,7,0
	CLRF TRISD,0
	RETURN

    READ_VALUE
	MOVFF ADRESH,readH
	MOVFF ADRESL,readL
	RETURN
	
    DISPLAY_NOTE
	MOVFF position, WREG
	ADDLW 0x10
	MOVWF TBLPTRL,0
	CLRF TBLPTRH,0
	CLRF TBLPTRU,0
	
	TBLRD*
	MOVFF TABLAT,LATD
	RETURN
	
    High_PWM
	;MOVFF TMR0L,LATD
	MOVFF TMR0L,readL
	MOVFF TMR0H,readH
	MOVFF timeNoteH, WREG
	CPFSEQ readH,0
	GOTO High_PWM
	MOVFF timeNoteL, WREG
	CPFSGT readL,0
	GOTO High_PWM
	RETURN
	
    MOVE_SERVO_X
	BCF INTCON, GIE ;disable interrupts
	BTFSS flags,4,0
	GOTO START_PWM_X
	MOVFF TMR0H, savedTimeH
	MOVFF TMR0L, savedTimeL
	;MOVFF savedTimeH,LATD
	;MOVFF savedTimeL,LATA
	
	START_PWM_X
	CLRF TMR0H,0
	CLRF TMR0L,0
	BSF LATC,0,0
	CALL High_PWM
	
	TIME_AT_LOW_X
	BCF LATC,0,0
	MOVFF TMR0L,readL
	MOVFF TMR0H,readH
	MOVLW HIGH(.5000)
	CPFSGT readH,0
	GOTO TIME_AT_LOW_X
	MOVLW LOW(.5000)
	CPFSGT readL,0
	GOTO TIME_AT_LOW_X
	
	
	BTFSS flags,3,0 ; re-enable interrupts only when we are not playing a song through the automatic mode
	BSF INTCON, GIE ; re-enable interrupts
	
	BTFSS flags,4,0
	RETURN
	CALL INIT_TMR0_REC_IDLE
	CLRF TMR0L,0
	CLRF TMR0H,0
	MOVF savedTimeL,0,0
	MOVWF TMR0L,0
	MOVLW .20
	ADDWF TMR0L,1,0	
	BTFSC STATUS, DC,0
	INCF savedTimeH,1,0
	MOVF savedTimeH,0,0
	MOVWF TMR0H,0
	
	
	RETURN
	
    MOVE_SERVO_Y
	BCF INTCON, GIE ;disable interrupts
	
	CLRF TMR0H,0
	CLRF TMR0L,0
	BSF LATC,1,0
	TIME_AT_HIGH_Y
	MOVFF TMR0L,readL
	MOVFF TMR0H,readH
	MOVFF timeNoteH, WREG
	CPFSEQ readH,0
	GOTO TIME_AT_HIGH_Y
	MOVFF timeNoteL, WREG
	CPFSGT readL,0
	GOTO TIME_AT_HIGH_Y
	
	TIME_AT_LOW_Y
	BCF LATC,1,0
	MOVFF TMR0L,readL
	MOVFF TMR0H,readH
	MOVLW HIGH(.5000)
	CPFSGT readH,0
	GOTO TIME_AT_LOW_Y
	MOVLW LOW(.5000)
	CPFSGT readL,0
	GOTO TIME_AT_LOW_Y
	
	BTFSS flags,3,0 ; re-enable interrupts only when we are not playing a song through the automatic mode
	BSF INTCON, GIE ; re-enable interrupts
	RETURN
	
    CHECK_LEFT
	
	; checking if < 100
	MOVLW .1
	CPFSLT readH,0
	RETURN
	MOVLW LOW(.100)
	CPFSLT readL,0
	RETURN
	; threshold for centre is left < 100
	
	MOVLW .0
	SUBWF position,0
	BTFSS STATUS,Z,0
	DECF position,1
	
	BSF flags,0,0
	
	CLRF timeNoteL,0
	CLRF timeNoteH,0
	CALL CONVERT_POS_TIME
	CALL MOVE_SERVO_X
	
	RETURN
	
    CHECK_RIGHT
	;checking if > 900
	MOVLW .2
	CPFSGT readH,0
	RETURN
	MOVLW LOW(.900)
	CPFSGT readL,0
	RETURN
	
	; threshold for centre is 900 < right
	
	MOVLW .7
	SUBWF position,0
	BTFSS STATUS,Z,0
	INCF position,1
	BSF flags,0,0
	
	CLRF timeNoteL,0
	CLRF timeNoteH,0
	CALL CONVERT_POS_TIME
	CALL MOVE_SERVO_X
	
	RETURN
	
    CHECK_CENTRE
	;checking if > 450
	MOVLW .1
	CPFSEQ readH,0
	GOTO check2
	MOVLW .200
	CPFSGT readL,0
	RETURN
	GOTO middle
	
	; checking if < 550
	check2
	MOVLW .2
	CPFSEQ readH,0
	RETURN
	MOVLW .50
	CPFSLT readL,0
	RETURN
	
	; threshold for centre is 450< centre <550
	middle
	BCF flags,0,0
	
	RETURN
	
    CHECK_REC_TIME
	;checking if > 60000
	MOVFF TMR0H,readH
	MOVFF TMR0L,readL
	;MOVFF readH,LATA
	MOVLW HIGH(.60000) ; 11101010
	CPFSLT readH,0
	GOTO GT_OR_EQ_60
	RETURN
	
	GT_OR_EQ_60
	;BSF LATA,2,0
	CPFSGT readH,0
	GOTO EQ_60
	BSF flags,5,0
	RETURN
	
	EQ_60
	;BSF LATA,3,0
	MOVLW LOW(.60000)  ; 01100000
	CPFSLT readL,0
	BSF flags,5,0
	;BSF LATA,4,0
	RETURN
	
    MAIN
	CALL INIT_OSC
	CALL INIT_PORTS
	CALL INIT_TMR0_PWM
	CALL INIT_INTERRUPTS
	CALL INIT_EUSART
	
	

	GOTO MAIN_LOOP
	
    MAIN_LOOP
    
	BTFSS flags,3,0
	CALL MANUAL_LOOP
	
	BTFSC flags,3,0
	CALL AUTOMATIC_LOOP
	
	GOTO MAIN_LOOP
	
    
    AUTOMATIC_LOOP
	BSF INTCON, GIE ; re-enable interrupts
	
	; setting the color of the RGB LED
	BCF LATC,3,0 
	BCF LATC,2,0
	BSF LATC,4,0
	
	WAIT_RX_AUTO
	    BTFSC flags,7,0
	    CALL MAIN_LOOP
	    BTFSS flags,3,0
	    GOTO MAIN_LOOP
	    BTFSS PIR1, RCIF, 0
	    GOTO WAIT_RX_AUTO
	MOVFF RCREG,readL
	
	BTFSC RCSTA,OERR,0 
	CALL OVERRUN
	
	MOVLW 'P'
	SUBWF readL,0
	BTFSS STATUS,Z,0
	GOTO AUTOMATIC_LOOP
	
	BCF INTCON, GIE ;disable interrupts
	
	MOVLW 'K'
	MOVWF TXREG,0
	WAIT_TRANS_AUTO
	    BTFSS TXSTA, 1, 0
	    GOTO WAIT_TRANS_AUTO
	    
	
	
	WAIT_RX_AUTO_PLAY
	    BTFSS PIR1, RCIF, 0
	    GOTO WAIT_RX_AUTO_PLAY
	
	
	MOVFF RCREG,readL ; contains the letter sent by the java interface
	MOVLW 'S'
	SUBWF readL,0
	BTFSC STATUS,Z,0
	GOTO AUTOMATIC_LOOP
	
	BTFSC RCSTA,OERR,0 
	CALL OVERRUN
	
	
	CLRF timeNoteL,0
	CLRF timeNoteH,0
	CALL CONVERT_Letter_Pos
	
	CALL MOVE_SERVO_X
	
	CALL HIT_NOTE
	GOTO WAIT_RX_AUTO_PLAY
	RETURN
	
	
    MANUAL_LOOP
	; setting the color of the RGB LED
	CALL RECORD_PRESSED
	
	BTFSC flags,4,0
	GOTO REC_LEDS
	BCF LATC,2,0
	BSF LATC,3,0
	BCF LATC,4,0
	GOTO SELECT_MODE
	
	REC_LEDS
	BSF LATC,2,0
	BCF LATC,3,0
	BCF LATC,4,0
	SELECT_MODE
	BTFSS flags,1,0
	CALL MANUAL_JOYSTICK
	
	BTFSC flags,1,0
	CALL MANUAL_JAVA
	
	RETURN
	
    MANUAL_JOYSTICK
	BCF LATC,5,0
	
	
	BTFSC flags,4,0
	CALL CHECK_REC_TIME
	BTFSC flags,5,0 ; checks if 60 seconds have passed since the recording started
	CALL REC_OVER
	
	BTFSC PORTB,3,0
	GOTO HIT_NOT_PRESSED
	
	CALL DEBOUNCE_LOOP
	BTFSC PORTB,3,0 ; if pushbutton is still pressed after debouncing loop then proceed, if not return
	GOTO HIT_NOT_PRESSED
	
	BTFSC flags,2,0
	GOTO NO_HIT
	
	CALL HIT_NOTE
	BSF flags,2,0
	GOTO NO_HIT
	
	HIT_NOT_PRESSED
	BCF flags,2,0
	NO_HIT
	
        BSF ADCON0,1,0 ;convert
        JOYSTICK_INTERNAL_LOOP
	BTFSC ADCON0,1,0
	GOTO JOYSTICK_INTERNAL_LOOP
	
        CALL READ_VALUE
	CALL CHECK_CENTRE
	
	BTFSS flags,0,0
	CALL CHECK_LEFT
	
	BTFSS flags,0,0
	CALL CHECK_RIGHT
	
	
	RETURN
	
	
    	
     MANUAL_JAVA
	BSF LATC,5,0
	WAIT_RX
	    BTFSC flags,4,0
	    SETF LATD,0
	    
	    BTFSS flags,4,0
	    CLRF LATD,0
	    
	    CALL RECORD_PRESSED
	    
	    BTFSC flags,4,0 ;
	    CALL CHECK_REC_TIME
	    BTFSC flags,5,0 ; checks if 60 seconds have passed since the recording started
	    CALL REC_OVER
	    
	    BTFSC flags,3,0 ; checks if mode has changed to automatic
	    GOTO MAIN_LOOP
	    
	    BTFSS flags,1,0 ; checks if manual mode has changed to joystick
	    GOTO MANUAL_LOOP
	    BTFSS PIR1, RCIF, 0
	    GOTO WAIT_RX
	
	
	MOVFF RCREG,readL ; contains the letter sent by the java interface
	;MOVFF readL,LATD
	
	BTFSC RCSTA,OERR,0 
	CALL OVERRUN
	
	
	;CLRF timeNoteL,0
	;CLRF timeNoteH,0
	CALL CONVERT_Letter_Pos
	CLRF timeNoteL,0
	CLRF timeNoteH,0
	CALL CONVERT_POS_TIME
	
	
	;MOVFF TMR0L,LATD
	CALL MOVE_SERVO_X
	
	
	CALL HIT_NOTE
	
	RETURN
	
    OVERRUN
	BCF RCSTA,CREN,0
	BSF CREN,0
	RETURN

    HIT_NOTE
	
	CALL DISPLAY_NOTE
	BTFSC flags,4,0
	CALL SAVE_NOTE
	BTFSC flags,4,0
	CALL INIT_TMR0_PWM
	
	BCF INTCON, GIE ;disable interrupts
	
	;lower hit arm
	MOVLW HIGH(.475)
	MOVWF timeNoteH,0
	MOVLW LOW(.475)
	MOVWF timeNoteL,0
	
	CALL MOVE_SERVO_Y
	
	;raise hit arm
	MOVLW HIGH(.125)
	MOVWF timeNoteH,0
	MOVLW LOW(.125)
	MOVWF timeNoteL,0
	
	CALL MOVE_SERVO_Y
	
	BTFSC flags,4,0
	CALL INIT_TMR0_REC_IDLE
	CLRF TMR0H,0
	CLRF TMR0L,0
	
	BSF INTCON, GIE ; re-enable interrupts
	RETURN
	
    REC_OVER
    
	BTFSC flags,6,0
	CALL SEND_STOP_JAVA
	BCF flags,6,0
	
	MOVLW b'00001000'
	MOVWF position,0
	CALL SAVE_NOTE
	
	
	BCF flags,4,0
	BCF flags,5,0
	;BSF LATD,7,0
	
	BCF LATC,2,0
	BSF LATC,3,0
	BCF LATC,4,0
	
	LFSR 0, Position_RAM_Notes
	LFSR 1, Position_RAM_Delay_L
	LFSR 2, Position_RAM_Delay_H
	CALL INIT_TMR0_PWM
	
	
    SEND_STOP_JAVA
	;BSF LATD,7,0
	MOVLW 'S'
	MOVWF TXREG,0
	WAIT_TRANS_STOP_REC
	    BTFSS TXSTA, 1, 0
	    GOTO WAIT_TRANS_STOP_REC
	BCF flags,6,0
	RETURN
	
    SAVE_NOTE
	
	BCF INTCON, GIE ; disable interrupts
	;MOVLW 0x001
	;MOVWF BSR,0  ; selecting bank 1 of the ram
	MOVFF TMR0H, readH
	MOVFF TMR0L, readL
	
	
	MOVFF position,INDF0
	
	
	MOVFF readL,INDF1
	
	
	MOVFF readH,INDF2
	
	
	INCF FSR0L,1
	INCF FSR1L,1
	INCF FSR2L,1
	
	BTFSC flags,6,0
	CALL SEND_NOTE    
	    
	CLRF TMR0H,0
	CLRF TMR0L,0
	BSF INTCON, GIE ; re-enable interrupts
	RETURN
	
    SEND_NOTE
	CALL CONVERT_POS_TIME
	;MOVFF noteLetter,LATA
	MOVFF noteLetter,TXREG
	WAIT_TRANS_NOTE_LETTER
	    BTFSS TXSTA, 1, 0
	    GOTO WAIT_TRANS_NOTE_LETTER
	    
	
	MOVFF readL, TXREG
	WAIT_TRANS_DELAY_L
	    BTFSS TXSTA, 1, 0
	    GOTO WAIT_TRANS_DELAY_L
	    
	MOVFF readH, TXREG
	WAIT_TRANS_DELAY_H
	    BTFSS TXSTA, 1, 0
	    GOTO WAIT_TRANS_DELAY_H
	RETURN
	
    CONVERT_POS_TIME
	MOVLW .0
	SUBWF position,0
	BTFSC STATUS,Z,0
	CALL SET_NOTE_C
	
	MOVLW .1
	SUBWF position,0
	BTFSC STATUS,Z,0
	CALL SET_NOTE_D
	
	MOVLW .2
	SUBWF position,0
	BTFSC STATUS,Z,0
	CALL SET_NOTE_E
	
	MOVLW .3
	SUBWF position,0
	BTFSC STATUS,Z,0
	CALL SET_NOTE_F
	
	MOVLW .4
	SUBWF position,0
	BTFSC STATUS,Z,0
	CALL SET_NOTE_G
	
	MOVLW .5
	SUBWF position,0
	BTFSC STATUS,Z,0
	CALL SET_NOTE_A
	
	MOVLW .6
	SUBWF position,0
	BTFSC STATUS,Z,0
	CALL SET_NOTE_B
	
	MOVLW .7
	SUBWF position,0
	BTFSC STATUS,Z,0
	CALL SET_NOTE_C2
	RETURN
	
    CONVERT_Letter_Pos
	MOVLW 'C'	; proteus reads the eusart input as b'1111111' or .255
	SUBWF readL,0
	BTFSC STATUS,Z,0
	CALL SET_NOTE_C
	
	MOVLW 'D'
	SUBWF readL,0
	BTFSC STATUS,Z,0
	CALL SET_NOTE_D
	
	MOVLW 'E'
	SUBWF readL,0
	BTFSC STATUS,Z,0
	CALL SET_NOTE_E
	
	MOVLW 'F'
	SUBWF readL,0
	BTFSC STATUS,Z,0
	CALL SET_NOTE_F
	
	MOVLW 'G'
	SUBWF readL,0
	BTFSC STATUS,Z,0
	CALL SET_NOTE_G
	
	MOVLW 'A'
	SUBWF readL,0
	BTFSC STATUS,Z,0
	CALL SET_NOTE_A
	
	MOVLW 'B'
	SUBWF readL,0
	BTFSC STATUS,Z,0
	CALL SET_NOTE_B
	
	MOVLW 'c'
	SUBWF readL,0
	BTFSC STATUS,Z,0
	CALL SET_NOTE_C2
	RETURN
	
    SET_NOTE_C
	MOVLW 'C'
	MOVWF noteLetter,0
	MOVLW .0
	MOVWF position,0
	MOVLW HIGH(.125)
	MOVWF timeNoteH,0
	MOVLW LOW(.125)
	MOVWF timeNoteL,0
	RETURN
	
    SET_NOTE_D
	MOVLW 'D'
	MOVWF noteLetter,0
	MOVLW .1
	MOVWF position,0
	MOVLW HIGH(.175)
	MOVWF timeNoteH,0
	MOVLW LOW(.175)
	MOVWF timeNoteL,0
	RETURN
    SET_NOTE_E
	MOVLW 'E'
	MOVWF noteLetter,0
	MOVLW .2
	MOVWF position,0
	MOVLW HIGH(.225)
	MOVWF timeNoteH,0
	MOVLW LOW(.225)
	MOVWF timeNoteL,0
	RETURN
	
    SET_NOTE_F
	MOVLW 'F'
	MOVWF noteLetter,0
	MOVLW .3
	MOVWF position,0
	MOVLW HIGH(.275)
	MOVWF timeNoteH,0
	MOVLW LOW(.275)
	MOVWF timeNoteL,0
	RETURN
    SET_NOTE_G
	MOVLW 'G'
	MOVWF noteLetter,0
	MOVLW .4
	MOVWF position,0
	MOVLW HIGH(.325)
	MOVWF timeNoteH,0
	MOVLW LOW(.325)
	MOVWF timeNoteL,0
	RETURN
    SET_NOTE_A
	MOVLW 'A'
	MOVWF noteLetter,0
	MOVLW .5
	MOVWF position,0
	MOVLW HIGH(.375)
	MOVWF timeNoteH,0
	MOVLW LOW(.375)
	MOVWF timeNoteL,0
	RETURN
    SET_NOTE_B
	MOVLW 'B'
	MOVWF noteLetter,0
	MOVLW .6
	MOVWF position,0
	MOVLW HIGH(.425)
	MOVWF timeNoteH,0
	MOVLW LOW(.425)
	MOVWF timeNoteL,0
	RETURN
	
    SET_NOTE_C2
	MOVLW 'c'
	MOVWF noteLetter,0
	MOVLW .7
	MOVWF position,0
	MOVLW HIGH(.475)
	MOVWF timeNoteH,0
	MOVLW LOW(.475)
	MOVWF timeNoteL,0
	RETURN
    
END
    