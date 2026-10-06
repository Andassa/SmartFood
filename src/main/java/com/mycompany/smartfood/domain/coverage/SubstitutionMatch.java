package com.mycompany.smartfood.domain.coverage;

import com.mycompany.smartfood.domain.Measures.Amount;
import com.mycompany.smartfood.domain.Substitutions;
import com.mycompany.smartfood.domain.Substitutions.Route;

final class SubstitutionMatch extends LineHandler {

    SubstitutionMatch(LineHandler next) {
        super(next);
    }

    @Override
    Match attempt(Request request) {
        Route route = Substitutions.route(request.name(), request.pantry(), request.need());
        if (route == null) {
            return null;
        }
        Amount alternative = new Amount(request.need().dimension(), request.need().base() * route.ratio());
        return request.pantry().take(route.stockKey(), alternative) ? Match.swapped(route.note()) : null;
    }
}
