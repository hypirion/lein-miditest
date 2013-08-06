(ns leiningen.miditest
  (:import (javax.sound.midi MidiSystem Sequencer MidiEvent ShortMessage
                             Sequence Track MetaEventListener
                             MetaMessage)))

(def default-note 60)
(def default-velocity 128)
(def default-duration 1000)
(def meta-end-of-track 47)

(defn play-note [channel duration]
  (.noteOn channel default-note default-velocity)
  (Thread/sleep duration)
  (.noteOff channel default-note))

(defn midi-event
  ([command channel data1 data2]
     (midi-event command channel data1 data2 1))
  ([command channel data1 data2 ticks]
     (MidiEvent.
      (doto (ShortMessage.)
        (.setMessage command channel data1 data2))
      ticks)))

(defn play-note-events
  [note]
  [(midi-event ShortMessage/NOTE_ON 1 note 127)
   (midi-event ShortMessage/NOTE_OFF 1 note 127 8)])

(defn find-instrument
  ([instrument-name]
     (with-open [synth (doto (MidiSystem/getSynthesizer) .open)]
       (find-instrument synth instrument-name)))
  ([synth instrument-name]
     (first (filter #(= instrument-name (.getName %))
                    (.getAvailableInstruments synth)))))

(defn change-instrument-events
  [instrument-name]
  (let [instrument (find-instrument instrument-name)
        instrument-int (.. instrument getPatch getProgram)]
    [(midi-event ShortMessage/PROGRAM_CHANGE 1 instrument-int 0)]))

(defn play-instrument
  [instrument-name]
  (let [instr-notes (change-instrument-events instrument-name)
        play-notes (play-note-events 60)
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
    (Thread/sleep 1000))) ; TODO: Get away from this somehow.

(defn miditest
  "I play the french horn."
  [project & args]
  (play-instrument "French Horn"))

