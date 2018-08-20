<?php
/*
Copyright 2007 GroundWork Open Source, Inc. ("GroundWork")
All rights reserved. Use is subject to GroundWork commercial license terms. 
*/


// Test widgets

class WidgetsTestWidget extends GuavaWidget  {
	
	public function Draw() {
		?>
		My Frames Being Enabled? <?=(string)$this->isFrames();?>
		<br />
		<div align="center">Test Widget Being Drawn with overwritten Draw()</div>
		<br />
		<?php
	}
	
}

class WidgetsImageWidgetConfigureDialog extends GuavaWidgetConfigureDialog {
	
	private $imagePath;
	
	public function __construct($source) {
		parent::__construct($source);
		
		$this->imagePath = new InputText(50, 255, $this->getSource()->getImageSource());
	}
	
	public function getImageSource() {
		return $this->imagePath->getValue();
	}
	
	public function Draw() {
		?>
		<h1>Specify The Image Source URL:</h1>
		<br />
		<?=$this->imagePath->Draw();?><br />
		<br />
		<?php
	}
}

class WidgetsImageWidget extends GuavaWidget implements ActionListener {
	
	private $image;
	
	public function init() {
		$this->image = new Image("");
		$this->setConfigClass("WidgetsImageWidgetConfigureDialog");
	}
	
	public function actionPerformed($event) {
		$this->image->setSrc($event->getSource()->getImageSource());
		$event->getSource()->hide();
		$event->getSource()->unregister();
	}
	
	public function getImageSource() {
		return $this->image->getSrc();
	}
	
	public function loadConfig($configObject) {
		$this->image->setSrc($configObject);
	}
	
	public function Draw() {
		$this->image->Draw();
	}
}

class WidgetsTestTemplateWidget extends GuavaWidget {
	
	private $myButton;
	private $clickCounter;
	
	protected function init() {
		$this->clickCounter = 0;
		$this->targetData("counter", (string)$this->clickCounter);
		$this->myButton = new Button("Click Me");
		$this->myButton->addClickListener("click", $this, "clickHandler");
		$this->bind("myButton", $this->myButton);
		$this->setTemplate(GUAVA_FS_ROOT . 'packages/widgets/templates/test.xml');
	}
	
	public function clickHandler($guavaObject, $parameter = null) {
		$this->targetData("counter", (string)++$this->clickCounter);
	}
}

class RSSReaderWidgetItem extends GuavaObject {
	private $title, $link, $description, $pubDate;
	
	public function __construct($title, $link, $description, $pubDate) {
		parent::__construct(GUAVA_FS_ROOT . 'packages/widgets/templates/rssitem.xml');
		
		$this->title = $title;
		$this->link = $link;
		$this->description = $description;
		$this->pubDate = $pubDate;
		
		$this->bind("self", $this);
	}
	
	public function getTitle() {
		return $this->title;
	}
	
	public function getLink() {
		return $this->link;
	}
	
	public function getDescription() {
		return $this->description;
	}
	
	public function getDate() {
		return $this->pubDate;
	}
	
}

class lastRSS {
    // -------------------------------------------------------------------
    // Public properties
    // -------------------------------------------------------------------
    var $default_cp = 'UTF-8';
    var $CDATA = 'nochange';
    var $cp = '';
    var $items_limit = 0;
    var $stripHTML = False;
    var $date_format = '';

    // -------------------------------------------------------------------
    // Private variables
    // -------------------------------------------------------------------
    var $channeltags = array ('title', 'link', 'description', 'language', 'copyright', 'managingEditor', 'webMaster', 'lastBuildDate', 'rating', 'docs');
    var $itemtags = array('title', 'link', 'description', 'author', 'category', 'comments', 'enclosure', 'guid', 'pubDate', 'source');
    var $imagetags = array('title', 'url', 'link', 'width', 'height');
    var $textinputtags = array('title', 'description', 'name', 'link');

    // -------------------------------------------------------------------
    // Parse RSS file and returns associative array.
    // -------------------------------------------------------------------
    function Get ($rss_url) {
        // If CACHE ENABLED
        if ($this->cache_dir != '') {
            $cache_file = $this->cache_dir . '/rsscache_' . md5($rss_url);
            $timedif = @(time() - filemtime($cache_file));
            if ($timedif < $this->cache_time) {
                // cached file is fresh enough, return cached array
                $result = unserialize(join('', file($cache_file)));
                // set 'cached' to 1 only if cached file is correct
                if ($result) $result['cached'] = 1;
            } else {
                // cached file is too old, create new
                $result = $this->Parse($rss_url);
                $serialized = serialize($result);
                if ($f = @fopen($cache_file, 'w')) {
                    fwrite ($f, $serialized, strlen($serialized));
                    fclose($f);
                }
                if ($result) $result['cached'] = 0;
            }
        }
        // If CACHE DISABLED >> load and parse the file directly
        else {
            $result = $this->Parse($rss_url);
            if ($result) $result['cached'] = 0;
        }
        // return result
        return $result;
    }
    
    // -------------------------------------------------------------------
    // Modification of preg_match(); return trimed field with index 1
    // from 'classic' preg_match() array output
    // -------------------------------------------------------------------
    function my_preg_match ($pattern, $subject) {
        // start regullar expression
        preg_match($pattern, $subject, $out);

        // if there is some result... process it and return it
        if(isset($out[1])) {
            // Process CDATA (if present)
            if ($this->CDATA == 'content') { // Get CDATA content (without CDATA tag)
                $out[1] = strtr($out[1], array('<![CDATA['=>'', ']]>'=>''));
            } elseif ($this->CDATA == 'strip') { // Strip CDATA
                $out[1] = strtr($out[1], array('<![CDATA['=>'', ']]>'=>''));
            }

            // If code page is set convert character encoding to required
            if ($this->cp != '')
                //$out[1] = $this->MyConvertEncoding($this->rsscp, $this->cp, $out[1]);
                $out[1] = iconv($this->rsscp, $this->cp.'//TRANSLIT', $out[1]);
            // Return result
            return trim($out[1]);
        } else {
        // if there is NO result, return empty string
            return '';
        }
    }

    // -------------------------------------------------------------------
    // Replace HTML entities &something; by real characters
    // -------------------------------------------------------------------
    function unhtmlentities ($string) {
        // Get HTML entities table
        $trans_tbl = get_html_translation_table (HTML_ENTITIES, ENT_QUOTES);
        // Flip keys<==>values
        $trans_tbl = array_flip ($trans_tbl);
        // Add support for &apos; entity (missing in HTML_ENTITIES)
        $trans_tbl += array('&apos;' => "'");
        // Replace entities by values
        return strtr ($string, $trans_tbl);
    }

    // -------------------------------------------------------------------
    // Parse() is private method used by Get() to load and parse RSS file.
    // Don't use Parse() in your scripts - use Get($rss_file) instead.
    // -------------------------------------------------------------------
    function Parse ($rss_url) {
        // Open and load RSS file
        if ($f = @fopen($rss_url, 'r')) {
            $rss_content = '';
            while (!feof($f)) {
                $rss_content .= fgets($f, 4096);
            }
            fclose($f);

            // Parse document encoding
            $result['encoding'] = $this->my_preg_match("'encoding=[\'\"](.*?)[\'\"]'si", $rss_content);
            // if document codepage is specified, use it
            if ($result['encoding'] != '')
                { $this->rsscp = $result['encoding']; } // This is used in my_preg_match()
            // otherwise use the default codepage
            else
                { $this->rsscp = $this->default_cp; } // This is used in my_preg_match()

            // Parse CHANNEL info
            preg_match("'<channel.*?>(.*?)</channel>'si", $rss_content, $out_channel);
            foreach($this->channeltags as $channeltag)
            {
                $temp = $this->my_preg_match("'<$channeltag.*?>(.*?)</$channeltag>'si", $out_channel[1]);
                if ($temp != '') $result[$channeltag] = $temp; // Set only if not empty
            }
            // If date_format is specified and lastBuildDate is valid
            if ($this->date_format != '' && ($timestamp = strtotime($result['lastBuildDate'])) !==-1) {
                        // convert lastBuildDate to specified date format
                        $result['lastBuildDate'] = date($this->date_format, $timestamp);
            }

            // Parse TEXTINPUT info
            preg_match("'<textinput(|[^>]*[^/])>(.*?)</textinput>'si", $rss_content, $out_textinfo);
                // This a little strange regexp means:
                // Look for tag <textinput> with or without any attributes, but skip truncated version <textinput /> (it's not beggining tag)
            if (isset($out_textinfo[2])) {
                foreach($this->textinputtags as $textinputtag) {
                    $temp = $this->my_preg_match("'<$textinputtag.*?>(.*?)</$textinputtag>'si", $out_textinfo[2]);
                    if ($temp != '') $result['textinput_'.$textinputtag] = $temp; // Set only if not empty
                }
            }
            // Parse IMAGE info
            preg_match("'<image.*?>(.*?)</image>'si", $rss_content, $out_imageinfo);
            if (isset($out_imageinfo[1])) {
                foreach($this->imagetags as $imagetag) {
                    $temp = $this->my_preg_match("'<$imagetag.*?>(.*?)</$imagetag>'si", $out_imageinfo[1]);
                    if ($temp != '') $result['image_'.$imagetag] = $temp; // Set only if not empty
                }
            }
            // Parse ITEMS
            preg_match_all("'<item(| .*?)>(.*?)</item>'si", $rss_content, $items);
            $rss_items = $items[2];
            $i = 0;
            $result['items'] = array(); // create array even if there are no items
            foreach($rss_items as $rss_item) {
                // If number of items is lower then limit: Parse one item
                if ($i < $this->items_limit || $this->items_limit == 0) {
                    foreach($this->itemtags as $itemtag) {
                        $temp = $this->my_preg_match("'<$itemtag.*?>(.*?)</$itemtag>'si", $rss_item);
                        if ($temp != '') $result['items'][$i][$itemtag] = $temp; // Set only if not empty
                    }
                    // Strip HTML tags and other bullshit from DESCRIPTION
                    if ($this->stripHTML && $result['items'][$i]['description'])
                        $result['items'][$i]['description'] = strip_tags($this->unhtmlentities(strip_tags($result['items'][$i]['description'])));
                    // Strip HTML tags and other bullshit from TITLE
                    if ($this->stripHTML && $result['items'][$i]['title'])
                        $result['items'][$i]['title'] = strip_tags($this->unhtmlentities(strip_tags($result['items'][$i]['title'])));
                    // If date_format is specified and pubDate is valid
                    if ($this->date_format != '' && ($timestamp = strtotime($result['items'][$i]['pubDate'])) !==-1) {
                        // convert pubDate to specified date format
                        $result['items'][$i]['pubDate'] = date($this->date_format, $timestamp);
                    }
                    // Item counter
                    $i++;
                }
            }

            $result['items_count'] = $i;
            return $result;
        }
        else // Error in opening return False
        {
            return False;
        }
    }
} 

class RSSReaderWidget extends GuavaWidget {
	private $url;
	
	private $rssReader;
	
	private $buffer;
	
	private $lastTimestamp;
	
	public function init() {
		$this->setTemplate(GUAVA_FS_ROOT . 'packages/widgets/templates/rssreader.xml');
		$this->bind("self", $this);
		
		$this->url = "http://rss.cnn.com/rss/cnn_topstories.rss";
		
		$this->buffer = new ScrollBuffer(5);
		$this->bind("buffer", $this->buffer);
		
		$this->rssReader = new lastRSS;
		
		$this->readRSS();
		
		$this->targetData("errormsg", "");
		
	}
	
	public function getURL() {
		return $this->url;
	}
	
	public function reload() {
		$this->readRSS();
	}
	
	public function Draw() {
		$this->buffer->Draw();
	}
	
	public function readRSS() {
		if($rs = $this->rssReader->get($this->url)) {

			for($counter = (count($rs['items']) - 1); $counter >= 0; $counter--) {
				if(strtotime($rs['items'][$counter]['pubDate']) > $this->lastTimestamp) {
					$this->lastTimestamp = strtotime($rs['items'][$counter]['pubDate']);
					$this->buffer->prepend(new RSSReaderWidgetItem($rs['items'][$counter]['title'], $rs['items'][$counter]['link'], $rs['items'][$counter]['description'], $rs['items'][$counter]['pubDate']));
				}
			}
		}
		else {
			$this->targetData("errormsg", "Error:  RSS Feed At " + $this->url + " Not Found.");
		}
	}
}



?>