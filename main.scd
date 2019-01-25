(s.waitForBoot({
	//GENERAL GUI VARIABLESs
	var w, w_width, w_height, screen_size, prop_height, prop_width,
	sidebar_width, instGrid_width, instGrid, instScroll, buttonTemplateAdd,
	//TEMPO VARIABLES
	tempoKnob, clockRes,
	//REVERB VARIABLES
	drywetKnob, roomKnob, reverb,
	//INSTRUMENT VARIABLES
	slot1, slot2, slot3, slot4, slot5, slot6, slot7, slot8;

	screen_size = Window.screenBounds;

	//horizontal and vertical percentage of the screen to be occupied by the window
	prop_width = 0.70;
	prop_height = 0.75;

	w_width = (prop_width * screen_size.width).round;
	w_height = (prop_height * screen_size.height).round;

	sidebar_width = (0.2 * w_width).round;

	w = Window("Euclidean AlgoRhythmic Sequencer", Rect(
		(screen_size.width /2) - (w_width/2),
		(screen_size.height /2) - (w_height/2) ,
		w_width,
		w_height));
	w.alwaysOnTop = true;                         //For Developing purposes, it's better not to swap back and forth from code to window

	//backgound image....not working
	//var a = Image.new(SCDoc.helpSourceDir +/+ "GUI Background.jpg");
	//w.view.backgroundImage_(a);

	SoundsLoader.load();
	//0.1.wait;
	/*-----------------------------------------TEMPO------------------------------------------------*/

	//clockRes = 4; //clock in 16th
	clockRes = 256; //clock in 1024th

	g = ControlSpec.new(40, 240, \linear, 1);
	tempoKnob = EZKnob(w, Rect((0.85*w_width).round, (0.01*w_height).round, (prop_height*w_height*0.125).round, (prop_height*0.125*w_height).round),"bpm", g, initVal:120);

	tempoKnob.action_({
		var tmp = tempoKnob.value;
		TempoClock.default.tempo_( clockRes * (tmp / 60));
		//tmp.postln;
	});

	/*-----------------------------------------REVERBERATION------------------------------------------------*/
	reverb = Synth.tail(s,\reverb);

	g = ControlSpec.new(0, 1, \lin);
	drywetKnob = EZKnob(w, Rect((0.81*w_width).round, (0.15*w_height).round, (prop_height*w_height*0.125).round, (prop_height*0.125*w_height).round),"reverb", g, initVal:0);

	drywetKnob.action_({
		var drywet = drywetKnob.value;
		reverb.set(\mix, drywet);
	});

	g = ControlSpec.new(0, 0.8, \lin);
	roomKnob = EZKnob(w, Rect((0.90*w_width).round, (0.15*w_height).round, (prop_height*w_height*0.125).round, (prop_height*0.125*w_height).round),"room-size", g, initVal:0.5);

	roomKnob.action_({
		var room = roomKnob.value;
		reverb.set(\room, room);
	});

	/*----------------------------------INSTRUMENTS GRID--------------------------------------------*/

	instGrid_width = w_width - sidebar_width;

	instGrid = ScrollView.new(w,Rect(0,0, instGrid_width, w_height) );
	instGrid.background = Color.fromHexString("b7c5d6");
	instGrid.decorator = FlowLayout(Rect(0, 0, instGrid_width, w_height) ); // adds a Grid layout for all the instuments

	/*------------------------------------Button Instrument Template--------------------------------*/

	buttonTemplateAdd = Button(w, Rect((0.81*w_width).round, (0.12*w_height).round, (0.18*w_width).round, (0.02*w_height).round));
	buttonTemplateAdd.string = "Add instrument";
	buttonTemplateAdd.action_({
		TemplateInstGUI.new.create( instGrid);
	});

	/*-----------------------------------INSTUMENTS GUI----------------------------------------------------*/

	slot1 = TemplateInstGUI.new.create(instGrid);
	slot2 = TemplateInstGUI.new.create(instGrid);
	slot3 = TemplateInstGUI.new.create(instGrid);
	slot4 = TemplateInstGUI.new.create(instGrid);
	slot5 = TemplateInstGUI.new.create(instGrid);
	slot6 = TemplateInstGUI.new.create(instGrid);
	slot7 = TemplateInstGUI.new.create(instGrid);
	slot8 = TemplateInstGUI.new.create(instGrid);


	/*--------------------------------END INSRTUMENTS GUI----------------------------------------------------*/

	//instGrid.decorator.nextLine;

	w.front;

	w.onClose={s.freeAll; Pdef.removeAll};


})
)
s.plotTree;
Platform.userExtensionDir;

