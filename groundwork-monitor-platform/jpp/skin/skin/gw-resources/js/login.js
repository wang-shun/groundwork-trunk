/******************************************************************************
 * JBoss, a division of Red Hat                                               *
 * Copyright 2009, Red Hat Middleware, LLC, and individual                    *
 * contributors as indicated by the @authors tag. See the                     *
 * copyright.txt in the distribution for a full listing of                    *
 * individual contributors.                                                   *
 *                                                                            *
 * This is free software; you can redistribute it and/or modify it            *
 * under the terms of the GNU Lesser General Public License as                *
 * published by the Free Software Foundation; either version 2.1 of           *
 * the License, or (at your option) any later version.                        *
 *                                                                            *
 * This software is distributed in the hope that it will be useful,           *
 * but WITHOUT ANY WARRANTY; without even the implied warranty of             *
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the GNU           *
 * Lesser General Public License for more details.                            *
 *                                                                            *
 * You should have received a copy of the GNU Lesser General Public           *
 * License along with this software; if not, write to the Free                *
 * Software Foundation, Inc., 51 Franklin St, Fifth Floor, Boston, MA         *
 * 02110-1301 USA, or see the FSF site: http://www.fsf.org.                   *
 ******************************************************************************/

if (typeof JBossPortal == 'undefined') {
    var JBossPortal = {
        login : { }
    };
}

JBossPortal.login = function() {

    //namespace login functions and variables
    var isModal = true;

    /**
     * Delay for a number of milliseconds
     */
    function sleep(delay)
    {
        var start = new Date().getTime();
        while (new Date().getTime() < start + delay);
    }

    return{
        /**
         * Gets the current display status of the login box (modal or page) sets global variable and
         * performs dynamic rendering options for login box
         *
         * @public
         * @type Function
         * @name displayStatus
         * @return {void}
         */
        displayStatus : function() {
            if (window.parent.frames.length >= 1)
            {
                //loaded as modal
            }
            else
            {
                isModal = false;
                try
                {
                    if (document.getElementById('login-content').scrollHeight > 0)
                    {
                        document.body.style.paddingTop = ((document.documentElement.clientHeight / 2) - document.getElementById('login-content').scrollHeight) + 'px';
                    }
                }
                catch(e)
                {
                    //swallow
                }
                //give the cancel button back button functionality (hackish) but there is no other way
                //to get returning page
                document.getElementById('login-cancel').onclick = function()
                {
                    self.history.go(-1);
                };
                document.getElementById('login-submit').style.right = '';
            }
        },

        /**
         * set focus on username
         *
         * @public
         * @type Function
         * @name setFocusOnLoginForm
         * @return {void}
         */
        setFocusOnLoginForm : function() {
            try
            {
                document.loginform.j_username.focus();
                highlightField(document.getElementById('j_username'));
            }
            catch (e)
            {
            }

        },

        /**
         * simple validate login fields and disable submit
         *
         * @public
         * @type Function
         * @name validate
         * @return {void}
         */
        validate : function(delay) {
            try
            {
                //delay for cached browser credentials
                if (delay != undefined) {
                    sleep(delay);
                }
                if (document.getElementById('j_username').value.length < 1 || document.getElementById('j_password').value.length < 1) {
                    document.getElementById('login-submit').disabled = true;
                    document.getElementById('login-submit').className = 'login-button disabled-button';
                } else {
                    document.getElementById('login-submit').disabled = false;
                    document.getElementById('login-submit').className = 'login-button';
                }
            }
            catch (e)
            {
            }

        },

        highlight : function(id) {
            var el = document.getElementById(id);
            if (el.className != 'highlight') {
                el.className = 'highlight';
            }
            else {
                el.className = '';
            }
        }
    };

}();
