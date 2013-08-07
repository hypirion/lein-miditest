(ns leiningen.miditest
  (:import (javax.sound.midi MidiSystem Sequencer MidiEvent ShortMessage
                             Sequence Track MetaEventListener MetaMessage)))

(def default-note 60)
(def default-velocity 128)
(def default-duration 1000)
(def ^:const meta-end-of-track 47)

(defn midi-event
  "Returns a new midi event set up to last n ticks (by default, 1)."
  ([command channel data1 data2]
     (midi-event command channel data1 data2 1))
  ([command channel data1 data2 ticks]
     (MidiEvent.
      (doto (ShortMessage.)
        (.setMessage command channel data1 data2))
      ticks)))

(defn play-note-events
  "Returns all midi events needed to play a 8 tick long note."
  [note]
  [(midi-event ShortMessage/NOTE_ON 1 note 127)
   (midi-event ShortMessage/NOTE_OFF 1 note 127 8)])

(defn find-instrument
  "Returns the instrument with the instrument-name given, or nil if none exits.
  If no syntesizer is applied, the system's default synthesizer will be used."
  ([instrument-name]
     (with-open [synth (doto (MidiSystem/getSynthesizer) .open)]
       (find-instrument synth instrument-name)))
  ([synth instrument-name]
     (first (filterv #(= instrument-name (.getName %))
                     (.getAvailableInstruments synth)))))

(defn change-instrument-events
  "Returns all the midi events needed to shift from one instrument to another."
  [instrument-name]
  (let [instrument (find-instrument instrument-name)
        instrument-int (.. instrument getPatch getProgram)]
    [(midi-event ShortMessage/PROGRAM_CHANGE 1 instrument-int 0)]))

(defn play-instrument
  "Plays a midi instrument with the given note from the system's default
  sequencer. Will block until the note has been played."
  [instrument-name note]
  (let [instr-notes (change-instrument-events instrument-name)
        play-notes (play-note-events note)
        player (doto (MidiSystem/getSequencer) .open)
        sequence (Sequence. Sequence/PPQ 4)
        track (. sequence createTrack)
        lock (Object.)
        event-listener (reify MetaEventListener
                           (meta [this e]
                             (if (== (.getType e) meta-end-of-track)
                               (locking lock
                                 (.notify lock)))))]
    (doseq [event (concat instr-notes play-notes)]
      (.add track event))
    (.setSequence player sequence)
    (.addMetaEventListener player event-listener)
    (.start player)
    (locking lock
      (while (.isRunning player)
        (.wait lock)))
    (Thread/sleep 500))) ; TODO: Get away from this somehow.

(defn all-instruments
  "Returns the name of all the different available instruments."
  []
  (with-open [synth (doto (MidiSystem/getSynthesizer) .open)]
    (mapv #(.getName %) (.getAvailableInstruments synth))))

(defn miditest
  "I play the french horn."
  [& _]
  (play-instrument "French Horn" 60))
