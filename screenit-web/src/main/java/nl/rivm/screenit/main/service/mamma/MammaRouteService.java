package nl.rivm.screenit.main.service.mamma;

/*-
 * ========================LICENSE_START=================================
 * screenit-web
 * %%
 * Copyright (C) 2012 - 2021 Facilitaire Samenwerking Bevolkingsonderzoek
 * %%
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU Affero General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 * 
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 * 
 * You should have received a copy of the GNU Affero General Public License
 * along with this program.  If not, see <http://www.gnu.org/licenses/>.
 * =========================LICENSE_END==================================
 */

import java.util.List;

import nl.rivm.screenit.model.InstellingGebruiker;
import nl.rivm.screenit.model.ScreeningOrganisatie;
import nl.rivm.screenit.model.mamma.MammaScreeningsEenheid;
import nl.rivm.screenit.model.mamma.MammaStandplaats;

public interface MammaRouteService
{

	void standplaatsenToevoegenRoute(MammaScreeningsEenheid screeningsEenheid, List<MammaStandplaats> standplaatsen, InstellingGebruiker ingelogdeInstellingGebruiker);

	String getCsvString(MammaScreeningsEenheid screeningsEenheid);

	List<MammaStandplaats> getStandplaatsenZonderRoute(ScreeningOrganisatie screeningOrganisatie);

	List<MammaStandplaats> getStandplaatsenMetRoute(ScreeningOrganisatie screeningOrganisatie);
}
