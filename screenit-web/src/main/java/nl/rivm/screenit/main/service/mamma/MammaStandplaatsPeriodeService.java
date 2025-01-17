package nl.rivm.screenit.main.service.mamma;

/*-
 * ========================LICENSE_START=================================
 * screenit-web
 * %%
 * Copyright (C) 2012 - 2022 Facilitaire Samenwerking Bevolkingsonderzoek
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

import nl.rivm.screenit.dto.mamma.afspraken.IMammaAfspraakWijzigenFilter;
import nl.rivm.screenit.dto.mamma.planning.PlanningStandplaatsPeriodeDto;
import nl.rivm.screenit.model.InstellingGebruiker;
import nl.rivm.screenit.model.ScreeningOrganisatie;
import nl.rivm.screenit.model.mamma.MammaScreeningsEenheid;
import nl.rivm.screenit.model.mamma.MammaStandplaats;
import nl.rivm.screenit.model.mamma.MammaStandplaatsPeriode;
import nl.rivm.screenit.model.mamma.MammaStandplaatsRonde;
import nl.rivm.screenit.model.verwerkingverslag.mamma.MammaStandplaatsRondeUitnodigenRapportage;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;

public interface MammaStandplaatsPeriodeService
{
	List<PlanningStandplaatsPeriodeDto> getStandplaatsPeriodesSorted(MammaScreeningsEenheid screeningsEenheid);

	List<MammaStandplaatsPeriode> getStandplaatsPeriodesVoorBulkVerzetten(ScreeningOrganisatie regio);

	void splitsStandplaatsPeriode(PlanningStandplaatsPeriodeDto standplaatsPeriode, InstellingGebruiker ingelogdeInstellingGebruiker);

	void updateSortList(int index, PlanningStandplaatsPeriodeDto item, MammaScreeningsEenheid screeningsEenheid, InstellingGebruiker ingelogdeInstellingGebruiker);

	boolean saveOrUpdateStandplaatsPeriode(PlanningStandplaatsPeriodeDto standplaatsPeriode, InstellingGebruiker ingelogdeInstellingGebruiker);

	MammaStandplaatsRondeUitnodigenRapportage getStandplaatsRondeUitnodigenRapportage(MammaStandplaatsRonde standplaatsRonde);

	List<MammaStandplaats> getStandplaatsenBuitenRegio(IMammaAfspraakWijzigenFilter filter, boolean verzetten);

	List<MammaScreeningsEenheid> getScreeningEenhedenBuitenRegio(IMammaAfspraakWijzigenFilter filter, boolean verzetten);

}
