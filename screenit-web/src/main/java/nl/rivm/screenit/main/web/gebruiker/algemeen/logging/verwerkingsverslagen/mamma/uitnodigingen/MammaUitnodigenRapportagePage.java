package nl.rivm.screenit.main.web.gebruiker.algemeen.logging.verwerkingsverslagen.mamma.uitnodigingen;

/*-
 * ========================LICENSE_START=================================
 * screenit-web
 * %%
 * Copyright (C) 2012 - 2020 Facilitaire Samenwerking Bevolkingsonderzoek
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

import nl.rivm.screenit.main.web.gebruiker.algemeen.AlgemeenPage;
import nl.rivm.screenit.main.web.security.SecurityConstraint;
import nl.rivm.screenit.model.enums.Actie;
import nl.rivm.screenit.model.enums.Bevolkingsonderzoek;
import nl.rivm.screenit.model.enums.Recht;
import nl.rivm.screenit.model.verwerkingverslag.mamma.MammaUitnodigenRapportage;

import org.apache.wicket.model.IModel;
import org.wicketstuff.shiro.ShiroConstraint;

@SecurityConstraint(
	actie = Actie.INZIEN,
	checkScope = false,
	constraint = ShiroConstraint.HasPermission,
	recht = Recht.GEBRUIKER_MAMMA_UITNODIGEN_VERWERKING_VERSLAG,
	bevolkingsonderzoekScopes = { Bevolkingsonderzoek.MAMMA })
public class MammaUitnodigenRapportagePage extends AlgemeenPage
{

	private static final long serialVersionUID = 1L;

	public MammaUitnodigenRapportagePage(IModel<MammaUitnodigenRapportage> model)
	{
		add(new MammaUitnodigenRapportagePanel("panel", model));
	}
}
