package com.mycompany.smartfood.domain.coverage;

import com.mycompany.smartfood.domain.Measures.Amount;
import com.mycompany.smartfood.domain.Pantry;

abstract class LineHandler {

    private final LineHandler next;

    LineHandler(LineHandler next) {
        this.next = next;
    }

    final Match handle(Request request) {
        Match match = attempt(request);
        return match != null ? match : forward(request);
    }

    abstract Match attempt(Request request);

    private Match forward(Request request) {
        return next == null ? Match.missing() : next.handle(request);
    }

    record Request(String name, Amount need, Pantry pantry) {
    }

    record Match(Kind kind, String note) {
        enum Kind { COVERED, SWAPPED, MISSING }

        static Match covered() {
            return new Match(Kind.COVERED, null);
        }

        static Match swapped(String note) {
            return new Match(Kind.SWAPPED, note);
        }

        static Match missing() {
            return new Match(Kind.MISSING, null);
        }
    }
}
