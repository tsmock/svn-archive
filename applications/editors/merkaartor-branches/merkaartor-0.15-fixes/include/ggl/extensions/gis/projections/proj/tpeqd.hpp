#ifndef GGL_PROJECTIONS_TPEQD_HPP
#define GGL_PROJECTIONS_TPEQD_HPP

// Generic Geometry Library - projections (based on PROJ4)
// This file is automatically generated. DO NOT EDIT.

// Copyright Barend Gehrels (1995-2009), Geodan Holding B.V. Amsterdam, the Netherlands.
// Copyright Bruno Lalande (2008-2009)
// Use, modification and distribution is subject to the Boost Software License,
// Version 1.0. (See accompanying file LICENSE_1_0.txt or copy at
// http://www.boost.org/LICENSE_1_0.txt)

// This file is converted from PROJ4, http://trac.osgeo.org/proj
// PROJ4 is originally written by Gerald Evenden (then of the USGS)
// PROJ4 is maintained by Frank Warmerdam
// PROJ4 is converted to Geometry Library by Barend Gehrels (Geodan, Amsterdam)

// Original copyright notice:
 
// Permission is hereby granted, free of charge, to any person obtaining a
// copy of this software and associated documentation files (the "Software"),
// to deal in the Software without restriction, including without limitation
// the rights to use, copy, modify, merge, publish, distribute, sublicense,
// and/or sell copies of the Software, and to permit persons to whom the
// Software is furnished to do so, subject to the following conditions:

// The above copyright notice and this permission notice shall be included
// in all copies or substantial portions of the Software.

// THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS
// OR IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF MERCHANTABILITY,
// FITNESS FOR A PARTICULAR PURPOSE AND NONINFRINGEMENT. IN NO EVENT SHALL
// THE AUTHORS OR COPYRIGHT HOLDERS BE LIABLE FOR ANY CLAIM, DAMAGES OR OTHER
// LIABILITY, WHETHER IN AN ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING
// FROM, OUT OF OR IN CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER
// DEALINGS IN THE SOFTWARE.

#include <boost/math/special_functions/hypot.hpp>

#include <ggl/extensions/gis/projections/impl/base_static.hpp>
#include <ggl/extensions/gis/projections/impl/base_dynamic.hpp>
#include <ggl/extensions/gis/projections/impl/projects.hpp>
#include <ggl/extensions/gis/projections/impl/factory_entry.hpp>

namespace ggl { namespace projection
{
    #ifndef DOXYGEN_NO_DETAIL
    namespace detail { namespace tpeqd{ 

            struct par_tpeqd
            {
                double cp1, sp1, cp2, sp2, ccs, cs, sc, r2z0, z02, dlam2;
                double hz0, thz0, rhshz0, ca, sa, lp, lamc;
            };

            // template class, using CRTP to implement forward/inverse
            template <typename Geographic, typename Cartesian, typename Parameters>
            struct base_tpeqd_spheroid : public base_t_fi<base_tpeqd_spheroid<Geographic, Cartesian, Parameters>,
                     Geographic, Cartesian, Parameters>
            {

                 typedef double geographic_type;
                 typedef double cartesian_type;

                par_tpeqd m_proj_parm;

                inline base_tpeqd_spheroid(const Parameters& par)
                    : base_t_fi<base_tpeqd_spheroid<Geographic, Cartesian, Parameters>,
                     Geographic, Cartesian, Parameters>(*this, par) {}

                inline void fwd(geographic_type& lp_lon, geographic_type& lp_lat, cartesian_type& xy_x, cartesian_type& xy_y) const
                {
                    double t, z1, z2, dl1, dl2, sp, cp;
                
                    sp = sin(lp_lat);
                    cp = cos(lp_lat);
                    z1 = aacos(this->m_proj_parm.sp1 * sp + this->m_proj_parm.cp1 * cp * cos(dl1 = lp_lon + this->m_proj_parm.dlam2));
                    z2 = aacos(this->m_proj_parm.sp2 * sp + this->m_proj_parm.cp2 * cp * cos(dl2 = lp_lon - this->m_proj_parm.dlam2));
                    z1 *= z1;
                    z2 *= z2;
                    xy_x = this->m_proj_parm.r2z0 * (t = z1 - z2);
                    t = this->m_proj_parm.z02 - t;
                    xy_y = this->m_proj_parm.r2z0 * asqrt(4. * this->m_proj_parm.z02 * z2 - t * t);
                    if ((this->m_proj_parm.ccs * sp - cp * (this->m_proj_parm.cs * sin(dl1) - this->m_proj_parm.sc * sin(dl2))) < 0.)
                        xy_y = -xy_y;
                }

                inline void inv(cartesian_type& xy_x, cartesian_type& xy_y, geographic_type& lp_lon, geographic_type& lp_lat) const
                {
                    double cz1, cz2, s, d, cp, sp;
                
                    cz1 = cos(boost::math::hypot(xy_y, xy_x + this->m_proj_parm.hz0));
                    cz2 = cos(boost::math::hypot(xy_y, xy_x - this->m_proj_parm.hz0));
                    s = cz1 + cz2;
                    d = cz1 - cz2;
                    lp_lon = - atan2(d, (s * this->m_proj_parm.thz0));
                    lp_lat = aacos(boost::math::hypot(this->m_proj_parm.thz0 * s, d) * this->m_proj_parm.rhshz0);
                    if ( xy_y < 0. )
                        lp_lat = - lp_lat;
                    /* lam--phi now in system relative to P1--P2 base equator */
                    sp = sin(lp_lat);
                    cp = cos(lp_lat);
                    lp_lat = aasin(this->m_proj_parm.sa * sp + this->m_proj_parm.ca * cp * (s = cos(lp_lon -= this->m_proj_parm.lp)));
                    lp_lon = atan2(cp * sin(lp_lon), this->m_proj_parm.sa * cp * s - this->m_proj_parm.ca * sp) + this->m_proj_parm.lamc;
                }
            };

            // Two Point Equidistant
            template <typename Parameters>
            void setup_tpeqd(Parameters& par, par_tpeqd& proj_parm)
            {
                double lam_1, lam_2, phi_1, phi_2, A12, pp;
                /* get control point locations */
                phi_1 = pj_param(par.params, "rlat_1").f;
                lam_1 = pj_param(par.params, "rlon_1").f;
                phi_2 = pj_param(par.params, "rlat_2").f;
                lam_2 = pj_param(par.params, "rlon_2").f;
                if (phi_1 == phi_2 && lam_1 == lam_2) throw proj_exception(-25);
                par.lam0 = adjlon(0.5 * (lam_1 + lam_2));
                proj_parm.dlam2 = adjlon(lam_2 - lam_1);
                proj_parm.cp1 = cos(phi_1);
                proj_parm.cp2 = cos(phi_2);
                proj_parm.sp1 = sin(phi_1);
                proj_parm.sp2 = sin(phi_2);
                proj_parm.cs = proj_parm.cp1 * proj_parm.sp2;
                proj_parm.sc = proj_parm.sp1 * proj_parm.cp2;
                proj_parm.ccs = proj_parm.cp1 * proj_parm.cp2 * sin(proj_parm.dlam2);
                proj_parm.z02 = aacos(proj_parm.sp1 * proj_parm.sp2 + proj_parm.cp1 * proj_parm.cp2 * cos(proj_parm.dlam2));
                proj_parm.hz0 = .5 * proj_parm.z02;
                A12 = atan2(proj_parm.cp2 * sin(proj_parm.dlam2),
                    proj_parm.cp1 * proj_parm.sp2 - proj_parm.sp1 * proj_parm.cp2 * cos(proj_parm.dlam2));
                proj_parm.ca = cos(pp = aasin(proj_parm.cp1 * sin(A12)));
                proj_parm.sa = sin(pp);
                proj_parm.lp = adjlon(atan2(proj_parm.cp1 * cos(A12), proj_parm.sp1) - proj_parm.hz0);
                proj_parm.dlam2 *= .5;
                proj_parm.lamc = HALFPI - atan2(sin(A12) * proj_parm.sp1, cos(A12)) - proj_parm.dlam2;
                proj_parm.thz0 = tan(proj_parm.hz0);
                proj_parm.rhshz0 = .5 / sin(proj_parm.hz0);
                proj_parm.r2z0 = 0.5 / proj_parm.z02;
                proj_parm.z02 *= proj_parm.z02;
                // par.inv = s_inverse;
                // par.fwd = s_forward;
                par.es = 0.;
            }

        }} // namespace detail::tpeqd
    #endif // doxygen 

    /*!
        \brief Two Point Equidistant projection
        \ingroup projections
        \tparam Geographic latlong point type
        \tparam Cartesian xy point type
        \tparam Parameters parameter type
        \par Projection characteristics
         - Miscellaneous
         - Spheroid
         - lat_1= lon_1= lat_2= lon_2=
        \par Example
        \image html ex_tpeqd.gif
    */
    template <typename Geographic, typename Cartesian, typename Parameters = parameters>
    struct tpeqd_spheroid : public detail::tpeqd::base_tpeqd_spheroid<Geographic, Cartesian, Parameters>
    {
        inline tpeqd_spheroid(const Parameters& par) : detail::tpeqd::base_tpeqd_spheroid<Geographic, Cartesian, Parameters>(par)
        {
            detail::tpeqd::setup_tpeqd(this->m_par, this->m_proj_parm);
        }
    };

    #ifndef DOXYGEN_NO_DETAIL
    namespace detail
    {

        // Factory entry(s)
        template <typename Geographic, typename Cartesian, typename Parameters>
        class tpeqd_entry : public detail::factory_entry<Geographic, Cartesian, Parameters>
        {
            public :
                virtual projection<Geographic, Cartesian>* create_new(const Parameters& par) const
                {
                    return new base_v_fi<tpeqd_spheroid<Geographic, Cartesian, Parameters>, Geographic, Cartesian, Parameters>(par);
                }
        };

        template <typename Geographic, typename Cartesian, typename Parameters>
        inline void tpeqd_init(detail::base_factory<Geographic, Cartesian, Parameters>& factory)
        {
            factory.add_to_factory("tpeqd", new tpeqd_entry<Geographic, Cartesian, Parameters>);
        }

    } // namespace detail 
    #endif // doxygen

}} // namespace ggl::projection

#endif // GGL_PROJECTIONS_TPEQD_HPP

