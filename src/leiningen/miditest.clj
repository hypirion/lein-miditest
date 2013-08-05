(ns leiningen.miditest
  (:import (javax.sound.midi MidiSystem Synthesizer)))

(def default-note 60)
(def default-velocity 128)
(def default-duration 1000)

(defn play-note [channel note-map]
  (let [{:keys [note velocity duration]
         :or {note default-note
              velocity default-velocity
              duration default-duration}} note-map]
    (. channel noteOn note velocity)
    (Thread/sleep duration)
    (. channel noteOff note)))

(defn find-instrument
  [synth instrument-name]
  (first (filter #(= instrument-name (.getName %))
                 (.getAvailableInstruments synth))))

(defn play-instrument
  [instrument-name]
  (with-open [synth (doto (MidiSystem/getSynthesizer) .open)]
    (let [channel (aget (.getChannels synth) 0)
          instrument (find-instrument synth instrument-name)]
      (when instrument
        (.loadInstrument synth instrument)
        (.programChange channel (.. instrument getPatch getProgram))
        (play-note channel {:duration 1000})
        (Thread/sleep 1000)))))

(defn miditest
  "I play the french horn."
  [project & args]
  (play-instrument "French Horn"))

